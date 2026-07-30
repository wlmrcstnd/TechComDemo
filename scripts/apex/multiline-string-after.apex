Account acc = new Account(Name = 'Acme Corp', AnnualRevenue = 5000000);
Contact ct = new Contact(FirstName = 'Juan', LastName = 'Dela Cruz');

String firstName = ct.FirstName;
String accountName = acc.Name;
Decimal revenue = acc.AnnualRevenue;

String emailBody = String.template('''
Hello ${firstName},

Thank you for your interest in ${accountName}.
Your assigned account value is: ${revenue}

Regards,
The Sales Team
''');

String jsonPayload = String.template('''
{
    "accountName": "${accountName}",
    "revenue": ${revenue}
}
''');

System.debug(emailBody);
System.debug(jsonPayload);
