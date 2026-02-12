#!/usr/bin/env python3
"""
Send Email mit ungehört. Signatur
"""
import smtplib
import os
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

def send_email_with_signature(to_email, subject, body_text):
    # Gmail SMTP Settings
    smtp_server = "smtp.gmail.com"
    smtp_port = 587
    sender_email = "scafa.assistant@gmail.com"
    
    # App-Passwort aus .env laden (falls vorhanden)
    app_password = os.getenv("GMAIL_APP_PASSWORD", "your-app-password-here")
    
    # Signatur laden
    sig_path = os.path.join(os.path.dirname(__file__), "ungehoert_signature.html")
    with open(sig_path, "r", encoding="utf-8") as f:
        signature_html = f.read()
    
    # Email erstellen
    msg = MIMEMultipart("alternative")
    msg["From"] = f"ungehört. <{sender_email}>"
    msg["To"] = to_email
    msg["Subject"] = subject
    
    # Plain Text
    msg.attach(MIMEText(body_text, "plain", "utf-8"))
    
    # HTML mit Signatur
    html_body = f"""
    <html>
    <body style="font-family: Arial, sans-serif; color: #333;">
        <p>{body_text.replace(chr(10), '<br>')}</p>
        <br>
        {signature_html}
    </body>
    </html>
    """
    msg.attach(MIMEText(html_body, "html", "utf-8"))
    
    # Senden
    with smtplib.SMTP(smtp_server, smtp_port) as server:
        server.starttls()
        server.login(sender_email, app_password)
        server.send_message(msg)
    
    print(f"✅ Email gesendet an {to_email}")

if __name__ == "__main__":
    import sys
    if len(sys.argv) < 4:
        print("Usage: python send_email_with_signature.py <to_email> <subject> <body_text>")
        sys.exit(1)
    
    send_email_with_signature(sys.argv[1], sys.argv[2], sys.argv[3])
