{{- define "microservice.configmap" }}
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ .Values.service.name }}-config
  labels:
    app: {{ .Values.service.name }}
    app.kubernetes.io/name: {{ .Values.service.name }}
data:
  {{- range $k, $v := .Values.config.data }}
  {{ $k }}: {{ $v | quote }}
  {{- end }}
{{- end }}
