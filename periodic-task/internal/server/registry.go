package server

import (
	"periodic-task/internal/conf"

	consul "github.com/go-kratos/kratos/contrib/registry/consul/v2"
	"github.com/go-kratos/kratos/v2/registry"
	"github.com/go-kratos/kratos/v2/transport/http"
	"github.com/hashicorp/consul/api"
)

// NewRegistrar 创建Consul注册器
func NewRegistrar(conf *conf.Registry) registry.Registrar {
	cfg := api.DefaultConfig()
	cfg.Address = conf.Consul.Address[0]
	cfg.Scheme = conf.Consul.Scheme

	client, err := api.NewClient(cfg)
	if err != nil {
		panic(err)
	}

	return consul.New(client)
}

func RegisterHealthCheck(srv *http.Server) {
	srv.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(200)
		w.Write([]byte(`{"status":"OK"}`))
	})
}
