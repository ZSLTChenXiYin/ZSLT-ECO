package biz

import (
	"periodic-task/internal/model"

	"github.com/go-kratos/kratos/v2/log"
)

type DataRepo interface {
	NewServiceOperator() *model.ServiceOperator
	NewMessageOperator() *model.MessageOperator
}

type DataUsecase struct {
	repo DataRepo
	log  *log.Helper
}

func NewDataUsecase(repo DataRepo, logger log.Logger) *DataUsecase {
	return &DataUsecase{
		repo: repo,
		log:  log.NewHelper(logger),
	}
}

func (uc *DataUsecase) NewServiceOperator() *model.ServiceOperator {
	return uc.repo.NewServiceOperator()
}

func (uc *DataUsecase) NewMessageOperator() *model.MessageOperator {
	return uc.repo.NewMessageOperator()
}
