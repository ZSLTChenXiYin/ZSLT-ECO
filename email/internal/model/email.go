package model

import (
	"context"
	"math/rand"
	"time"

	"errors"

	"gorm.io/datatypes"
	"gorm.io/gorm"
)

type Email struct {
	gorm.Model
	Email    string `gorm:"unique;not null"`
	Password string `gorm:"not null"`
}

func (Email) TableName() string { return "emails" }

type EmailTemplate struct {
	gorm.Model
	Name    string `gorm:"not null"`
	Args    datatypes.JSON
	Content string `gorm:"not null"`
}

func (EmailTemplate) TableName() string { return "email_templates" }

type EmailOperator struct {
	db *gorm.DB
}

func NewEmailOperator(db *gorm.DB) *EmailOperator {
	return &EmailOperator{db: db}
}

func (e *EmailOperator) CreateEmail(ctx context.Context, email *Email) error {
	return e.db.WithContext(ctx).Create(email).Error
}

func (e *EmailOperator) GetRandomEmail(ctx context.Context) (*Email, error) {
	email := &Email{}
	var count int64

	err := e.db.WithContext(ctx).Model(&Email{}).Count(&count).Error
	if err != nil {
		return nil, err
	}
	if count == 0 {
		return nil, errors.New("no email available")
	} else if count == 1 {
		err = e.db.WithContext(ctx).First(email).Error
		if err != nil {
			return nil, err
		}
		return email, nil
	}

	random := rand.New(rand.NewSource(time.Now().UnixNano()))
	offset := random.Int63n(count)

	err = e.db.WithContext(ctx).Offset(int(offset)).First(&email).Error
	if err != nil {
		return nil, err
	}

	return email, nil
}

func (e *EmailOperator) CreateEmailTemplate(ctx context.Context, template *EmailTemplate) error {
	return e.db.WithContext(ctx).Create(template).Error
}

func (e *EmailOperator) GetEmailTemplate(ctx context.Context, id uint) (*EmailTemplate, error) {
	template := &EmailTemplate{}
	err := e.db.WithContext(ctx).First(template, id).Error
	return template, err
}
