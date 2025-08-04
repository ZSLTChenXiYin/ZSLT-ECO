package model

import (
	"context"

	"gorm.io/gorm"
)

type Service struct {
	gorm.Model
	Name   string `gorm:"type:varchar(255);uniqueIndex:idx_name_secret;not null"`
	Secret string `gorm:"type:char(32);uniqueIndex:idx_name_secret;not null"`
	Scheme string `gorm:"not null"`
}

func (Service) TableName() string { return "services" }

type ServiceOperator struct {
	db *gorm.DB
}

func NewServiceOperator(db *gorm.DB) *ServiceOperator { return &ServiceOperator{db: db} }

func (s *ServiceOperator) ExistsService(name string, secret string) (bool, error) {
	var count int64
	err := s.db.Model(&Service{}).
		Where("name = ? AND secret = ?", name, secret).
		Count(&count).
		Error

	if err != nil {
		return false, err
	}
	return count > 0, nil
}

func (s *ServiceOperator) CreateService(ctx context.Context, service *Service) error {
	return s.db.WithContext(ctx).Create(service).Error
}

func (s *ServiceOperator) GetServiceList(ctx context.Context, index uint64, pages uint64) ([]Service, error) {
	var services []Service
	err := s.db.WithContext(ctx).Offset(int(calculateOffset(index, pages))).Limit(int(pages)).Find(services).Error
	return services, err
}

func (s *ServiceOperator) GetServiceListByName(ctx context.Context, index uint64, pages uint64, name string) ([]Service, error) {
	var services []Service
	err := s.db.WithContext(ctx).Where("name = ?", name).Offset(int(calculateOffset(index, pages))).Limit(int(pages)).Find(services).Error
	return services, err
}

func (s *ServiceOperator) DeleteServiceByName(ctx context.Context, name string, secret string) error {
	return s.db.WithContext(ctx).Where("name = ? and secret = ?", name, secret).Delete(Service{}).Error
}
