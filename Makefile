.PHONY: cluster-up cluster-down deploy build port-forward clean

CLUSTER_NAME ?= bank-dev
HELM_CHART ?= ./helm/bank-app
NAMESPACE ?= dev

cluster-up:
	kind create cluster --name $(CLUSTER_NAME) --config kind-config.yaml
	kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
	kubectl wait --namespace ingress-nginx \
		--for=condition=ready pod \
		--selector=app.kubernetes.io/component=controller \
		--timeout=90s

cluster-down:
	kind delete cluster --name $(CLUSTER_NAME)

build:
	./build-images.sh $(CLUSTER_NAME)

deploy: build
	helm dependency update $(HELM_CHART)
	helm upgrade --install bank-app $(HELM_CHART) \
		--namespace $(NAMESPACE) --create-namespace \
		--wait --timeout 10m

port-forward:
	kubectl port-forward -n $(NAMESPACE) svc/keycloak 8089:80 &
	kubectl port-forward -n $(NAMESPACE) svc/bank-front-ui 8080:80 &

clean:
	helm uninstall bank-app -n $(NAMESPACE) || true
	kubectl delete namespace $(NAMESPACE) --wait || true
