package biz

import (
	"context"
	"email/internal/model"

	"github.com/go-kratos/kratos/v2/log"
)

type DataRepo interface {
	NewEmailOperator() *model.EmailOperator
}

type EmailUsecase struct {
	email *model.EmailOperator
	log   *log.Helper
}

func NewEmailUsecase(repo DataRepo, logger log.Logger) *EmailUsecase {
	return &EmailUsecase{
		email: repo.NewEmailOperator(),
		log:   log.NewHelper(logger),
	}
}

func (uc *EmailUsecase) CreateEmail(ctx context.Context, email string, password string) error {
	return uc.email.CreateEmail(ctx, &model.Email{Email: email, Password: password})
}

func (uc *EmailUsecase) GetRandomEmail(ctx context.Context) (*model.Email, error) {
	return uc.email.GetRandomEmail(ctx)
}

func (uc *EmailUsecase) CreateEmailTemplate(ctx context.Context, name string, args []byte, content string) (template_id uint64, err error) {
	template := &model.EmailTemplate{
		Name:    name,
		Args:    args,
		Content: content,
	}
	err = uc.email.CreateEmailTemplate(ctx, template)
	if err != nil {
		uc.log.Error(err)
	}
	return uint64(template.ID), err
}

func (uc *EmailUsecase) GetEmailTemplate(ctx context.Context, id uint64) (*model.EmailTemplate, error) {
	return uc.email.GetEmailTemplate(ctx, uint(id))
}
