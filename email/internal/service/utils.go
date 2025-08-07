package service

import (
	"bytes"
	"encoding/json"
	"html/template"

	"email/internal/model"

	"gopkg.in/gomail.v2"
)

func sendEmail(host string, port uint32, from string, to string, subject string, use_default_args bool, args []byte, email *model.Email, tmpl *model.EmailTemplate) error {
	t := template.Must(template.New(tmpl.Name).Parse(tmpl.Content))

	var buf bytes.Buffer
	buf.WriteString(tmpl.Content)

	if use_default_args {
		if tmpl.Args != nil {
			default_args_map := map[string]any{}

			err := json.Unmarshal(tmpl.Args, &default_args_map)
			if err != nil {
				return err
			}

			buf.Reset()

			err = t.Execute(&buf, default_args_map)
			if err != nil {
				return err
			}
		}
	}

	if args != nil {
		args_map := map[string]any{}

		err := json.Unmarshal(args, &args_map)
		if err != nil {
			return err
		}

		buf.Reset()

		err = t.Execute(&buf, args_map)
		if err != nil {
			return err
		}
	}

	msg := gomail.NewMessage()
	msg.SetHeader("From", from)
	msg.SetHeader("To", to)
	msg.SetHeader("Subject", subject)
	msg.SetBody("text/html", buf.String())

	dialer := gomail.NewDialer(host, int(port), email.Email, email.Password)

	go func() {
		dialer.DialAndSend(msg)
	}()

	return nil
}
