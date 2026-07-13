.PHONY: cluster-up cluster-down deploy build clean

CLUSTER_NAME ?= bank-dev
HELM_CHART ?= ./helm/bank-app
NAMESPACE ?= dev

cluster-up:
	kind create cluster --name $(CLUSTER_NAME) --config kind-config.yaml
	kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
	@echo "Waiting ingress"
	@until kubectl wait --namespace ingress-nginx \
		--for=condition=ready pod \
		--selector=app.kubernetes.io/component=controller \
		--timeout=180s 2>/dev/null; do \
		echo " retrying..."; \
		sleep 3; \
	done

cluster-down:
	kind delete cluster --name $(CLUSTER_NAME)

build:
	./build-images.sh $(CLUSTER_NAME)

deploy: build
	helm dependency update $(HELM_CHART)
	helm upgrade --install bank-app $(HELM_CHART) \
		--namespace $(NAMESPACE) --create-namespace \
		--wait --timeout 20m

clean:
	helm uninstall bank-app -n $(NAMESPACE) || true
	kubectl delete namespace $(NAMESPACE) --wait || true
