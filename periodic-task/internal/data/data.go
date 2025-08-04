package data

import (
	"periodic-task/internal/biz"
	"periodic-task/internal/conf"
	"periodic-task/internal/model"

	"github.com/go-kratos/kratos/v2/log"
	"github.com/google/wire"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
)

// ProviderSet is data providers.
var ProviderSet = wire.NewSet(NewData, NewDatabase, NewGreeterRepo)

// Data .
type Data struct {
	db *gorm.DB
}

// NewData .
func NewData(c *conf.Data, logger log.Logger, db *gorm.DB) (*Data, func(), error) {
	cleanup := func() {
		log.NewHelper(logger).Info("closing the data resources")
	}
	return &Data{db: db}, cleanup, nil
}

func NewDatabase(c *conf.Data) (database *gorm.DB) {
	// 启动数据库
	switch c.Database.Driver {
	case "mysql":
		db, err := gorm.Open(mysql.Open(c.Database.Source), &gorm.Config{})
		if err != nil {
			panic(err)
		}
		database = db
	default:
		panic(c.Database.Driver + " database is not yet supported")
	}

	return
}

type dataRepo struct {
	data *Data
	log  *log.Helper
}

func NewDataRepo(data *Data, logger log.Logger) biz.DataRepo {
	err := data.db.AutoMigrate(&model.Service{})
	if err != nil {
		panic(err)
	}
	return &dataRepo{
		data: data,
		log:  log.NewHelper(logger),
	}
}

func (d *dataRepo) NewServiceOperator() *model.ServiceOperator {
	return model.NewServiceOperator(d.data.db)
}

func (d *dataRepo) NewMessageOperator() *model.MessageOperator {
	return model.NewMessageOperator(d.data.db)
}
