Account acc = new Account(Name = 'Acme Corp', AnnualRevenue = 5000000);
Contact ct = new Contact(FirstName = 'Juan', LastName = 'Dela Cruz');

String emailBody = 'Hello ' + ct.FirstName + ',\n\n' +
    'Thank you for your interest in ' + acc.Name + '.\n' +
    'Your assigned account value is: ' + acc.AnnualRevenue + '\n\n' +
    'Regards,\nThe Sales Team';

String jsonPayload = '{' +
    '"accountName": "' + acc.Name + '",' +
    '"revenue": ' + acc.AnnualRevenue +
    '}';

System.debug(emailBody);
System.debug(jsonPayload);
