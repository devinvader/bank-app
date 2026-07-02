{{- define "microservice.deployment" }}
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ .Values.service.name }}
  labels:
    app: {{ .Values.service.name }}
    app.kubernetes.io/name: {{ .Values.service.name }}
    app.kubernetes.io/part-of: bank-app
spec:
  replicas: {{ .Values.replicaCount }}
  selector:
    matchLabels:
      app: {{ .Values.service.name }}
  template:
    metadata:
      labels:
        app: {{ .Values.service.name }}
        app.kubernetes.io/name: {{ .Values.service.name }}
    spec:
      containers:
        - name: {{ .Values.service.name }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          ports:
            - containerPort: {{ .Values.service.targetPort }}
              name: http
          envFrom:
            - configMapRef:
                name: {{ .Values.service.name }}-config
{{- if .Values.secret }}
            - secretRef:
                name: {{ .Values.service.name }}-secret
{{- end }}
          startupProbe:
            httpGet:
              path: /actuator/health
              port: {{ .Values.service.targetPort }}
            initialDelaySeconds: 15
            periodSeconds: 5
            failureThreshold: 30
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: {{ .Values.service.targetPort }}
            initialDelaySeconds: 10
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: {{ .Values.service.targetPort }}
            initialDelaySeconds: 30
            periodSeconds: 20
{{- end }}
