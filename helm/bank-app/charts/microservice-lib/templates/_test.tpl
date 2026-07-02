{{- define "microservice.test" }}
apiVersion: v1
kind: Pod
metadata:
  name: "{{ .Values.service.name }}-test"
  annotations:
    "helm.sh/hook": test
spec:
  containers:
    - name: test
      image: curlimages/curl:latest
      command:
        - curl
        - -f
        - http://{{ .Values.service.name }}:{{ .Values.service.port }}/actuator/health
  restartPolicy: Never
{{- end }}
