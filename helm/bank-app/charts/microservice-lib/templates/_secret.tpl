{{- define "microservice.secret" }}
{{- if .Values.secret }}
apiVersion: v1
kind: Secret
metadata:
  name: {{ .Values.service.name }}-secret
  labels:
    app: {{ .Values.service.name }}
    app.kubernetes.io/name: {{ .Values.service.name }}
type: Opaque
data:
  {{- range $k, $v := .Values.secret.data }}
  {{ $k }}: {{ $v | b64enc | quote }}
  {{- end }}
{{- end }}
{{- end }}
