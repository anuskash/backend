# Gmail SMTP Setup for Email Verification

## Step 1: Generate Gmail App Password

1. **Go to Google Account Settings**
   - Visit: https://myaccount.google.com/
   - Click on "Security" in the left sidebar

2. **Enable 2-Step Verification** (if not already enabled)
   - Find "2-Step Verification" section
   - Click and follow the steps to enable it
   - **Note:** App Passwords only work if 2-Step Verification is enabled

3. **Create App Password**
   - After enabling 2-Step Verification, go back to Security
   - Find "App passwords" (or search for "App passwords" in the search bar)
   - Click on it
   - Select app: "Mail"
   - Select device: "Other (Custom name)"
   - Enter name: "UON Marketplace"
   - Click "Generate"
   - **Copy the 16-character password** (looks like: `abcd efgh ijkl mnop`)

## Step 2: Update application.properties

Open: `src/main/resources/application.properties`

Replace these lines:
```properties
spring.mail.username=YOUR_GMAIL_ADDRESS@gmail.com
spring.mail.password=YOUR_APP_PASSWORD_HERE
```

With your actual values:
```properties
spring.mail.username=anns79749@gmail.com
spring.mail.password=abcd efgh ijkl mnop
```

**Important:** Use the 16-character App Password, NOT your regular Gmail password!

## Step 3: Restart the Application

1. Stop the running application
2. Start it again
3. Try registering with a new email
4. You should receive the verification email in your inbox!

## Troubleshooting

### "Username and Password not accepted"
- Make sure you're using the App Password, not your regular password
- Verify 2-Step Verification is enabled
- Remove any spaces from the App Password

### "Connection timeout"
- Check your internet connection
- Some networks block port 587, try port 465 with SSL instead:
  ```properties
  spring.mail.port=465
  spring.mail.properties.mail.smtp.ssl.enable=true
  ```

### Still not working?
- Check Gmail's "Less secure app access" (though App Passwords should work without this)
- Verify your Gmail account doesn't have restrictions
- Check if your firewall is blocking outbound SMTP connections

## Security Note

⚠️ **Never commit your App Password to Git!**

Consider using environment variables in production:
```properties
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
```

Then set environment variables instead of hardcoding credentials.
