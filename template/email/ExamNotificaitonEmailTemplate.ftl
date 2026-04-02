<html>
<head>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f6f8;
            padding: 20px;
        }
        .container {
            background: #ffffff;
            padding: 20px;
            border-radius: 10px;
        }
        .header {
            font-size: 20px;
            font-weight: bold;
            color: #1f2937;
            margin-bottom: 15px;
        }
        .info {
            margin: 10px 0;
            color: #374151;
        }
        .box {
            background: #f9fafb;
            padding: 12px;
            border-radius: 8px;
            margin-top: 15px;
        }
        .footer {
            margin-top: 20px;
            font-size: 12px;
            color: #6b7280;
        }
    </style>
</head>

<body>
    <div class="container">

        <div class="header">
            📘 Exam Assigned: ${examName}
        </div>

        <div class="info">
            Hello,
        </div>

        <div class="info">
            You have been assigned an exam.
        </div>

        <div class="box">
            <p><b>Exam Name:</b> ${examName}</p>
            <p><b>Date:</b> ${examDate}</p>
            <p><b>Duration:</b> ${examDuration} minutes</p>
        </div>

        <#-- Loop users -->
        <#if assignedUsers??>
            <div class="box">
                <b>Login Details:</b>
                <ul>
                    <#list assignedUsers as user>
                        <li>
                            Username: ${user.username} <br/>
                            Password: ${user.password}
                        </li>
                    </#list>
                </ul>
            </div>
        </#if>

        <div class="info">
            Please log in and complete your exam on time.
        </div>

        <div class="footer">
            This is an automated email. Please do not reply.
        </div>

    </div>
</body>
</html>