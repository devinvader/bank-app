{{- define "microservice.service" }}
apiVersion: v1
kind: Service
metadata:
  name: {{ .Values.service.name }}
  labels:
    app: {{ .Values.service.name }}
    app.kubernetes.io/name: {{ .Values.service.name }}
    app.kubernetes.io/part-of: bank-app
spec:
  type: ClusterIP
  ports:
    - port: {{ .Values.service.port }}
      targetPort: {{ .Values.service.targetPort }}
      protocol: TCP
      name: http
  selector:
    app: {{ .Values.service.name }}
{{- end }}
