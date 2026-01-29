# Kubernetes Manifests

This guide provides production-ready Kubernetes manifests for deploying HLabMonitor in various configurations, from simple single-node clusters to highly available production setups.

---

## Prerequisites

- Kubernetes cluster (1.24+)
- `kubectl` configured and connected to your cluster
- Basic understanding of Kubernetes concepts (Deployments, Services, ConfigMaps, Secrets)

### For HomeLab Users

HLabMonitor works great on:
- **K3s** - Lightweight Kubernetes for Raspberry Pi and homelabs
- **MicroK8s** - Canonical's lightweight Kubernetes
- **Kind** - Kubernetes in Docker for testing
- **Minikube** - Local Kubernetes development
- **Talos Linux** - Immutable Kubernetes OS
- **Full Kubernetes** - Self-hosted or managed clusters

---

## Docker Image Variants

HLabMonitor is available in three image variants:

| Image Tag | Base Image | Size | Best For |
|-----------|------------|------|----------|
| `latest` or `ubuntu` | Eclipse Temurin 21 JRE (Ubuntu Jammy) | ~250MB | General use, maximum compatibility |
| `alpine` | Eclipse Temurin 21 JRE (Alpine) | ~180MB | Lightweight deployments, resource-constrained nodes |
| `corretto` | Amazon Corretto 21 (AL2023) | ~240MB | AWS EKS, Amazon Linux preference |

**Version pinning (recommended):**
- `wiserisk/hlabmonitor:1.0.0` - Ubuntu-based
- `wiserisk/hlabmonitor:1.0.0-alpine` - Alpine-based
- `wiserisk/hlabmonitor:1.0.0-corretto` - Corretto-based

---

## Table of Contents

- [Quick Start (H2)](#quick-start-h2)
- [SQLite Deployment](#sqlite-deployment)
- [PostgreSQL Deployment](#postgresql-deployment)
- [SQL Server Deployment](#sql-server-deployment)
- [Production Examples](#production-examples)
- [Advanced Configurations](#advanced-configurations)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)

---

## Quick Start (H2)

**Minimal deployment with embedded H2 database.** Perfect for testing.

### Namespace

**`namespace.yaml`:**

``` yaml
apiVersion: v1
kind: Namespace
metadata:
  name: hlabmonitor
```

### ConfigMap

**`configmap.yaml`:**

``` yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: hlabmonitor-config
  namespace: hlabmonitor
data:
  application.yaml: |
    monitoring:
      ping:
        gateway:
          target: 192.168.1.1
          interval: 1m
      http:
        google:
          target: www.google.com
          interval: 5m
```

### Deployment

**`deployment.yaml`:**

``` yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hlabmonitor
  namespace: hlabmonitor
  labels:
    app: hlabmonitor
spec:
  replicas: 1
  selector:
    matchLabels:
      app: hlabmonitor
  template:
    metadata:
      labels:
        app: hlabmonitor
    spec:
      containers:
      - name: hlabmonitor
        image: wiserisk/hlabmonitor:latest
        ports:
        - containerPort: 8080
          name: http
          protocol: TCP
        volumeMounts:
        - name: config
          mountPath: /etc/hlabmonitor
          readOnly: true
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 40
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
      volumes:
      - name: config
        configMap:
          name: hlabmonitor-config
```

### Service

**`service.yaml`:**

``` yaml
apiVersion: v1
kind: Service
metadata:
  name: hlabmonitor
  namespace: hlabmonitor
  labels:
    app: hlabmonitor
spec:
  type: ClusterIP
  ports:
  - port: 8080
    targetPort: 8080
    protocol: TCP
    name: http
  selector:
    app: hlabmonitor
```

### Deploy

``` bash
kubectl apply -f namespace.yaml
kubectl apply -f configmap.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

# Verify
kubectl -n hlabmonitor get pods
kubectl -n hlabmonitor get svc

# Port-forward to access
kubectl -n hlabmonitor port-forward svc/hlabmonitor 8080:8080
# Access: http://localhost:8080
```

> ⚠️ **Note**: H2 is in-memory. Data is lost when the pod restarts.

---

## SQLite Deployment

**Persistent file-based database.** Suitable for single-replica deployments.

### PersistentVolumeClaim

**`pvc.yaml`:**

``` yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: hlabmonitor-data
  namespace: hlabmonitor
spec:
  accessModes:
  - ReadWriteOnce
  resources:
    requests:
      storage: 5Gi
  # Optional: storageClassName for specific storage type
  # storageClassName: local-path  # K3s default
  # storageClassName: hostpath    # Minikube/Kind
```

### ConfigMap

**`configmap-sqlite.yaml`:**

``` yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: hlabmonitor-config
  namespace: hlabmonitor
data:
  application.yaml: |
    monitoring:
      ping:
        router:
          target: 192.168.1.1
          interval: 30s
        pihole:
          target: 192.168.1.10
          interval: 1m
      http:
        homeassistant:
          target: homeassistant.home.svc.cluster.local
          interval: 2m
          ssl: false
        proxmox:
          target: proxmox.local
          interval: 5m
          ssl: true
          certificate:
            verify: true
            interval: 1d
```

### Deployment with SQLite

**`deployment-sqlite.yaml`:**

``` yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hlabmonitor
  namespace: hlabmonitor
  labels:
    app: hlabmonitor
spec:
  replicas: 1  # Must be 1 for SQLite (no concurrent writes)
  strategy:
    type: Recreate  # Important for SQLite
  selector:
    matchLabels:
      app: hlabmonitor
  template:
    metadata:
      labels:
        app: hlabmonitor
    spec:
      containers:
      - name: hlabmonitor
        image: wiserisk/hlabmonitor:alpine  # Lightweight variant
        env:
        - name: DATABASE_TYPE
          value: "sqlite"
        - name: DATABASE_PATH
          value: "/data/monitor.db"
        ports:
        - containerPort: 8080
          name: http
        volumeMounts:
        - name: config
          mountPath: /etc/hlabmonitor
          readOnly: true
        - name: data
          mountPath: /data
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 40
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 10
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
      volumes:
      - name: config
        configMap:
          name: hlabmonitor-config
      - name: data
        persistentVolumeClaim:
          claimName: hlabmonitor-data
```

### Deploy

``` bash
kubectl apply -f namespace.yaml
kubectl apply -f pvc.yaml
kubectl apply -f configmap-sqlite.yaml
kubectl apply -f deployment-sqlite.yaml
kubectl apply -f service.yaml
```

---

## PostgreSQL Deployment

**Production-ready setup with PostgreSQL.** Recommended for high availability.

### PostgreSQL StatefulSet

**`postgres-statefulset.yaml`:**

``` yaml
apiVersion: v1
kind: Secret
metadata:
  name: postgres-secret
  namespace: hlabmonitor
type: Opaque
stringData:
  postgres-password: "change_me_in_production"
  hlabmonitor-password: "change_me_in_production"
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: postgres-config
  namespace: hlabmonitor
data:
  POSTGRES_DB: "hlabmonitor"
  POSTGRES_USER: "hlabmonitor_user"
---
apiVersion: v1
kind: Service
metadata:
  name: postgres
  namespace: hlabmonitor
  labels:
    app: postgres
spec:
  type: ClusterIP
  clusterIP: None  # Headless service for StatefulSet
  ports:
  - port: 5432
    targetPort: 5432
    name: postgres
  selector:
    app: postgres
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgres
  namespace: hlabmonitor
spec:
  serviceName: postgres
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
      - name: postgres
        image: postgres:16-alpine
        envFrom:
        - configMapRef:
            name: postgres-config
        env:
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgres-secret
              key: hlabmonitor-password
        - name: PGDATA
          value: /var/lib/postgresql/data/pgdata
        ports:
        - containerPort: 5432
          name: postgres
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
        livenessProbe:
          exec:
            command:
            - /bin/sh
            - -c
            - pg_isready -U hlabmonitor_user -d hlabmonitor
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          exec:
            command:
            - /bin/sh
            - -c
            - pg_isready -U hlabmonitor_user -d hlabmonitor
          initialDelaySeconds: 5
          periodSeconds: 5
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
  volumeClaimTemplates:
  - metadata:
      name: postgres-storage
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 10Gi
```

### HLabMonitor with PostgreSQL

**`hlabmonitor-postgres.yaml`:**

``` yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: hlabmonitor-config
  namespace: hlabmonitor
data:
  application.yaml: |
    monitoring:
      ping:
        gateway:
          target: 192.168.1.1
          interval: 30s
        dns-primary:
          target: 192.168.1.1
          interval: 1m
        dns-secondary:
          target: 8.8.8.8
          interval: 1m
      http:
        website:
          target: www.example.com
          interval: 5m
          ssl: true
          certificate:
            verify: true
            interval: 1d
        api:
          target: api.internal.local
          interval: 2m
          ssl: false
---
apiVersion: v1
kind: Secret
metadata:
  name: hlabmonitor-db-secret
  namespace: hlabmonitor
type: Opaque
stringData:
  database-username: "hlabmonitor_user"
  database-password: "change_me_in_production"
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hlabmonitor
  namespace: hlabmonitor
  labels:
    app: hlabmonitor
spec:
  replicas: 2  # Can scale with PostgreSQL
  selector:
    matchLabels:
      app: hlabmonitor
  template:
    metadata:
      labels:
        app: hlabmonitor
    spec:
      containers:
      - name: hlabmonitor
        image: wiserisk/hlabmonitor:1.0.0  # Pin version for production
        env:
        - name: DATABASE_TYPE
          value: "postgresql"
        - name: DATABASE_HOST
          value: "postgres.hlabmonitor.svc.cluster.local"
        - name: DATABASE_PORT
          value: "5432"
        - name: DATABASE_NAME
          value: "hlabmonitor"
        - name: DATABASE_USERNAME
          valueFrom:
            secretKeyRef:
              name: hlabmonitor-db-secret
              key: database-username
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: hlabmonitor-db-secret
              key: database-password
        ports:
        - containerPort: 8080
          name: http
        volumeMounts:
        - name: config
          mountPath: /etc/hlabmonitor
          readOnly: true
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
      volumes:
      - name: config
        configMap:
          name: hlabmonitor-config
---
apiVersion: v1
kind: Service
metadata:
  name: hlabmonitor
  namespace: hlabmonitor
  labels:
    app: hlabmonitor
spec:
  type: ClusterIP
  ports:
  - port: 8080
    targetPort: 8080
    name: http
  selector:
    app: hlabmonitor
```

### Deploy

``` bash
kubectl apply -f namespace.yaml
kubectl apply -f postgres-statefulset.yaml
kubectl apply -f hlabmonitor-postgres.yaml

# Wait for PostgreSQL to be ready
kubectl -n hlabmonitor wait --for=condition=ready pod -l app=postgres --timeout=120s

# Check status
kubectl -n hlabmonitor get pods
kubectl -n hlabmonitor get svc
```

---

## SQL Server Deployment

**For Windows-centric environments or existing SQL Server infrastructure.**

### SQL Server StatefulSet

**`sqlserver-statefulset.yaml`:**

``` yaml
apiVersion: v1
kind: Secret
metadata:
  name: sqlserver-secret
  namespace: hlabmonitor
type: Opaque
stringData:
  sa-password: "YourStrong@Password123"
---
apiVersion: v1
kind: Service
metadata:
  name: sqlserver
  namespace: hlabmonitor
  labels:
    app: sqlserver
spec:
  type: ClusterIP
  ports:
  - port: 1433
    targetPort: 1433
    name: sqlserver
  selector:
    app: sqlserver
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: sqlserver
  namespace: hlabmonitor
spec:
  serviceName: sqlserver
  replicas: 1
  selector:
    matchLabels:
      app: sqlserver
  template:
    metadata:
      labels:
        app: sqlserver
    spec:
      containers:
      - name: sqlserver
        image: mcr.microsoft.com/mssql/server:2022-latest
        env:
        - name: ACCEPT_EULA
          value: "Y"
        - name: SA_PASSWORD
          valueFrom:
            secretKeyRef:
              name: sqlserver-secret
              key: sa-password
        - name: MSSQL_PID
          value: "Express"
        ports:
        - containerPort: 1433
          name: sqlserver
        volumeMounts:
        - name: sqlserver-storage
          mountPath: /var/opt/mssql
        resources:
          requests:
            memory: "2Gi"
            cpu: "500m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
  volumeClaimTemplates:
  - metadata:
      name: sqlserver-storage
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 20Gi
```

### HLabMonitor with SQL Server

**`hlabmonitor-sqlserver.yaml`:**

``` yaml
apiVersion: v1
kind: Secret
metadata:
  name: hlabmonitor-db-secret
  namespace: hlabmonitor
type: Opaque
stringData:
  database-username: "sa"
  database-password: "YourStrong@Password123"
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hlabmonitor
  namespace: hlabmonitor
spec:
  replicas: 2
  selector:
    matchLabels:
      app: hlabmonitor
  template:
    metadata:
      labels:
        app: hlabmonitor
    spec:
      containers:
      - name: hlabmonitor
        image: wiserisk/hlabmonitor:corretto  # Corretto variant
        env:
        - name: DATABASE_TYPE
          value: "sqlserver"
        - name: DATABASE_HOST
          value: "sqlserver.hlabmonitor.svc.cluster.local"
        - name: DATABASE_PORT
          value: "1433"
        - name: DATABASE_NAME
          value: "HLabMonitor"
        - name: DATABASE_USERNAME
          valueFrom:
            secretKeyRef:
              name: hlabmonitor-db-secret
              key: database-username
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: hlabmonitor-db-secret
              key: database-password
        ports:
        - containerPort: 8080
          name: http
        volumeMounts:
        - name: config
          mountPath: /etc/hlabmonitor
          readOnly: true
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
      volumes:
      - name: config
        configMap:
          name: hlabmonitor-config
```

---

## Production Examples

### High Availability with PostgreSQL

**`ha-deployment.yaml`:**

``` yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hlabmonitor
  namespace: hlabmonitor
  labels:
    app: hlabmonitor
spec:
  replicas: 3  # Multiple replicas for HA
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 1
  selector:
    matchLabels:
      app: hlabmonitor
  template:
    metadata:
      labels:
        app: hlabmonitor
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - hlabmonitor
              topologyKey: kubernetes.io/hostname
      containers:
      - name: hlabmonitor
        image: wiserisk/hlabmonitor:1.0.0
        env:
        - name: DATABASE_TYPE
          value: "postgresql"
        - name: DATABASE_HOST
          value: "postgres.hlabmonitor.svc.cluster.local"
        - name: DATABASE_PORT
          value: "5432"
        - name: DATABASE_NAME
          value: "hlabmonitor"
        - name: DATABASE_USERNAME
          valueFrom:
            secretKeyRef:
              name: hlabmonitor-db-secret
              key: database-username
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: hlabmonitor-db-secret
              key: database-password
        - name: JAVA_OPTS
          value: "-Xmx768m -Xms512m"
        - name: LOGGING_LEVEL_ROOT
          value: "INFO"
        - name: LOGGING_LEVEL_BE_WISERISK_HLABMONITOR
          value: "DEBUG"
        ports:
        - containerPort: 8080
          name: http
        volumeMounts:
        - name: config
          mountPath: /etc/hlabmonitor
          readOnly: true
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        resources:
          requests:
            memory: "768Mi"
            cpu: "500m"
          limits:
            memory: "1536Mi"
            cpu: "1500m"
        securityContext:
          runAsNonRoot: true
          runAsUser: 1001
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: false
          capabilities:
            drop:
            - ALL
      volumes:
      - name: config
        configMap:
          name: hlabmonitor-config
---
apiVersion: v1
kind: Service
metadata:
  name: hlabmonitor
  namespace: hlabmonitor
  labels:
    app: hlabmonitor
spec:
  type: ClusterIP
  sessionAffinity: ClientIP
  ports:
  - port: 8080
    targetPort: 8080
    name: http
  selector:
    app: hlabmonitor
---
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: hlabmonitor-pdb
  namespace: hlabmonitor
spec:
  minAvailable: 1
  selector:
    matchLabels:
      app: hlabmonitor
```

### Ingress Configuration

**`ingress.yaml`:**

``` yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: hlabmonitor
  namespace: hlabmonitor
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - monitor.example.com
    secretName: hlabmonitor-tls
  rules:
  - host: monitor.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: hlabmonitor
            port:
              number: 8080
```

### Traefik Ingress

**`ingress-traefik.yaml`:**

``` yaml
aapiVersion: traefik.containo.us/v1alpha1
kind: IngressRoute
metadata:
  name: hlabmonitor
  namespace: hlabmonitor
spec:
  entryPoints:
  - websecure
  routes:
  - match: Host(`monitor.example.com`)
    kind: Rule
    services:
    - name: hlabmonitor
      port: 8080
  tls:
    certResolver: letsencrypt
```

---

## Advanced Configurations

### External PostgreSQL (Existing Database)

**`external-postgres.yaml`:**

``` yaml
apiVersion: v1
kind: Secret
metadata:
  name: hlabmonitor-db-secret
  namespace: hlabmonitor
type: Opaque
stringData:
  database-username: "hlabmonitor_user"
  database-password: "secure_password"
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: hlabmonitor-db-config
  namespace: hlabmonitor
data:
  DATABASE_TYPE: "postgresql"
  DATABASE_HOST: "postgres.external.example.com"  # External hostname
  DATABASE_PORT: "5432"
  DATABASE_NAME: "hlabmonitor"
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hlabmonitor
  namespace: hlabmonitor
spec:
  replicas: 2
  selector:
    matchLabels:
      app: hlabmonitor
  template:
    metadata:
      labels:
        app: hlabmonitor
    spec:
      containers:
      - name: hlabmonitor
        image: wiserisk/hlabmonitor:latest
        envFrom:
        - configMapRef:
            name: hlabmonitor-db-config
        env:
        - name: DATABASE_USERNAME
          valueFrom:
            secretKeyRef:
              name: hlabmonitor-db-secret
              key: database-username
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: hlabmonitor-db-secret
              key: database-password
        ports:
        - containerPort: 8080
        volumeMounts:
        - name: config
          mountPath: /etc/hlabmonitor
          readOnly: true
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
      volumes:
      - name: config
        configMap:
          name: hlabmonitor-config
```

### HorizontalPodAutoscaler

**`hpa.yaml`:**

``` yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: hlabmonitor
  namespace: hlabmonitor
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: hlabmonitor
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
      - type: Percent
        value: 100
        periodSeconds: 30
      - type: Pods
        value: 2
        periodSeconds: 30
      selectPolicy: Max
```

### NetworkPolicy (Security)

**`networkpolicy.yaml`:**

``` yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: hlabmonitor-policy
  namespace: hlabmonitor
spec:
  podSelector:
    matchLabels:
      app: hlabmonitor
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx  # Allow from ingress controller
    - podSelector:
        matchLabels:
          app: prometheus  # Allow from Prometheus
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: postgres  # Allow to PostgreSQL
    ports:
    - protocol: TCP
      port: 5432
  - to:
    - namespaceSelector: {}  # Allow DNS
    ports:
    - protocol: UDP
      port: 53
  - to:  # Allow external monitoring targets
    - namespaceSelector: {}
    ports:
    - protocol: TCP
      port: 443
    - protocol: TCP
      port: 80
    - protocol: ICMP
```

### ServiceMonitor (Prometheus)

**`servicemonitor.yaml`:**

``` yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: hlabmonitor
  namespace: hlabmonitor
  labels:
    app: hlabmonitor
spec:
  selector:
    matchLabels:
      app: hlabmonitor
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 30s
    scrapeTimeout: 10s
```

---

## Best Practices

### Security

1. **Use Secrets for sensitive data:**

``` yaml
# Never put passwords in ConfigMaps
apiVersion: v1
kind: Secret
metadata:
  name: hlabmonitor-db-secret
type: Opaque
stringData:
  database-password: "secure_password"  # Or use sealed-secrets, external-secrets
```

2. **Run as non-root (already configured):**

``` yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1001
  allowPrivilegeEscalation: false
  capabilities:
    drop:
    - ALL
```

3. **Use NetworkPolicies to restrict traffic**

4. **Pin image versions in production:**

``` yaml
image: wiserisk/hlabmonitor:1.0.0  # Not :latest
```

### Resource Management

1. **Set resource requests and limits:**

``` yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

2. **Use HPA for auto-scaling**

3. **Configure PodDisruptionBudget for HA:**

``` yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: hlabmonitor-pdb
spec:
  minAvailable: 1
  selector:
    matchLabels:
      app: hlabmonitor
```

### High Availability

1. **Run multiple replicas (2+ for PostgreSQL/SQL Server)**

2. **Use pod anti-affinity to spread across nodes:**

``` yaml
affinity:
  podAntiAffinity:
    preferredDuringSchedulingIgnoredDuringExecution:
    - weight: 100
      podAffinityTerm:
        labelSelector:
          matchLabels:
            app: hlabmonitor
        topologyKey: kubernetes.io/hostname
```

3. **Configure proper health checks**

### Persistence

1. **Use PersistentVolumes with proper storage classes**

2. **For SQLite, use ReadWriteOnce with single replica**

3. **For PostgreSQL, consider backup strategies:**

``` yaml
# Example CronJob for PostgreSQL backups
apiVersion: batch/v1
kind: CronJob
metadata:
  name: postgres-backup
  namespace: hlabmonitor
spec:
  schedule: "0 2 * * *"  # Daily at 2 AM
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: postgres:16-alpine
            env:
            - name: PGPASSWORD
              valueFrom:
                secretKeyRef:
                  name: postgres-secret
                  key: hlabmonitor-password
            command:
            - /bin/sh
            - -c
            - |
              pg_dump -h postgres -U hlabmonitor_user hlabmonitor | \
              gzip > /backups/hlabmonitor-$(date +%Y%m%d-%H%M%S).sql.gz
              # Keep only last 7 days
              find /backups -name "hlabmonitor-*.sql.gz" -mtime +7 -delete
            volumeMounts:
            - name: backups
              mountPath: /backups
          volumes:
          - name: backups
            persistentVolumeClaim:
              claimName: postgres-backups
          restartPolicy: OnFailure
```

---

## Troubleshooting

### Pod Not Starting

**Check pod status:**

``` bash
kubectl -n hlabmonitor get pods
kubectl -n hlabmonitor describe pod <pod-name>
kubectl -n hlabmonitor logs <pod-name>
```

**Common issues:**
- Image pull errors (check image tag)
- ConfigMap/Secret not found
- Insufficient resources
- Failed health checks

### Database Connection Errors

**Verify database is running:**

``` bash
kubectl -n hlabmonitor get pods -l app=postgres
kubectl -n hlabmonitor logs -l app=postgres
```

**Test database connection:**

``` bash
# PostgreSQL
kubectl -n hlabmonitor exec -it <postgres-pod> -- psql -U hlabmonitor_user -d hlabmonitor -c "SELECT 1;"

# SQL Server
kubectl -n hlabmonitor exec -it <sqlserver-pod> -- /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -Q "SELECT 1"
```

**Check service DNS:**

``` bash
kubectl -n hlabmonitor run -it --rm debug --image=busybox --restart=Never -- nslookup postgres.hlabmonitor.svc.cluster.local
```

### ConfigMap Not Applied

**Verify ConfigMap exists:**

``` bash
kubectl -n hlabmonitor get configmap
kubectl -n hlabmonitor describe configmap hlabmonitor-config
```

**Check mounted config:**

``` bash
kubectl -n hlabmonitor exec <pod-name> -- cat /etc/hlabmonitor/application.yaml
```

**Force pod restart after ConfigMap change:**

``` bash
kubectl -n hlabmonitor rollout restart deployment hlabmonitor
```

### Ingress Not Working

**Check ingress:**

``` bash
kubectl -n hlabmonitor get ingress
kubectl -n hlabmonitor describe ingress hlabmonitor
```

**Verify ingress controller:**

``` bash
kubectl get pods -n ingress-nginx
kubectl logs -n ingress-nginx <ingress-controller-pod>
```

**Test service directly:**

``` bash
kubectl -n hlabmonitor port-forward svc/hlabmonitor 8080:8080
curl http://localhost:8080/actuator/health
```

### High Memory Usage

**Check pod resource usage:**

``` bash
kubectl -n hlabmonitor top pods
```

**Adjust JVM memory:**

``` yaml
env:
- name: JAVA_OPTS
  value: "-Xmx512m -Xms256m"
```

**Increase resource limits:**

``` yaml
resources:
  limits:
    memory: "2Gi"
```

---

## Useful Commands

### Deployment Management

``` bash
# Apply all manifests
kubectl apply -f .

# Check deployment status
kubectl -n hlabmonitor get all

# Watch pod status
kubectl -n hlabmonitor get pods -w

# Rollout status
kubectl -n hlabmonitor rollout status deployment/hlabmonitor

# Rollback deployment
kubectl -n hlabmonitor rollout undo deployment/hlabmonitor

# Scale deployment
kubectl -n hlabmonitor scale deployment hlabmonitor --replicas=3
```

### Logs and Debugging

``` bash
# View logs
kubectl -n hlabmonitor logs -f deployment/hlabmonitor

# View logs from specific container
kubectl -n hlabmonitor logs <pod-name> -c hlabmonitor

# View previous container logs (after crash)
kubectl -n hlabmonitor logs <pod-name> --previous

# Execute shell in pod
kubectl -n hlabmonitor exec -it <pod-name> -- sh

# Port forward for local access
kubectl -n hlabmonitor port-forward svc/hlabmonitor 8080:8080
```

### Configuration Management

``` bash
# View ConfigMap
kubectl -n hlabmonitor get configmap hlabmonitor-config -o yaml

# Edit ConfigMap
kubectl -n hlabmonitor edit configmap hlabmonitor-config

# Restart pods after config change
kubectl -n hlabmonitor rollout restart deployment/hlabmonitor

# View Secret (base64 encoded)
kubectl -n hlabmonitor get secret hlabmonitor-db-secret -o yaml

# Decode secret
kubectl -n hlabmonitor get secret hlabmonitor-db-secret -o jsonpath='{.data.database-password}' | base64 -d
```

### Cleanup

``` bash
# Delete all resources in namespace
kubectl delete namespace hlabmonitor

# Delete specific resources
kubectl -n hlabmonitor delete deployment hlabmonitor
kubectl -n hlabmonitor delete service hlabmonitor
kubectl -n hlabmonitor delete pvc hlabmonitor-data
```

---

## Complete Example: All-in-One

For quick deployment, combine everything in one file:

**`hlabmonitor-complete.yaml`:**

``` yaml
---
apiVersion: v1
kind: Namespace
metadata:
  name: hlabmonitor
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: hlabmonitor-config
  namespace: hlabmonitor
data:
  application.yaml: |
    monitoring:
      ping:
        gateway:
          target: 192.168.1.1
          interval: 1m
      http:
        website:
          target: www.example.com
          interval: 5m
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hlabmonitor
  namespace: hlabmonitor
spec:
  replicas: 1
  selector:
    matchLabels:
      app: hlabmonitor
  template:
    metadata:
      labels:
        app: hlabmonitor
    spec:
      containers:
      - name: hlabmonitor
        image: wiserisk/hlabmonitor:latest
        ports:
        - containerPort: 8080
        volumeMounts:
        - name: config
          mountPath: /etc/hlabmonitor
          readOnly: true
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 40
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 10
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
      volumes:
      - name: config
        configMap:
          name: hlabmonitor-config
---
apiVersion: v1
kind: Service
metadata:
  name: hlabmonitor
  namespace: hlabmonitor
spec:
  type: ClusterIP
  ports:
  - port: 8080
    targetPort: 8080
  selector:
    app: hlabmonitor
```

**Deploy:**

``` bash
kubectl apply -f hlabmonitor-complete.yaml
kubectl -n hlabmonitor port-forward svc/hlabmonitor 8080:8080
```

---

## See Also

- [Application YAML Reference](../configuration/application-yaml.md) - Configuration options
- [Database Setup](../configuration/database.md) - Database-specific guides
- [Environment Variables](../configuration/environment-variables.md) - Environment variable reference
- [Docker Compose Examples](docker-compose.md) - Docker deployment
- [Production Checklist](production-checklist.md) - Production readiness guide
