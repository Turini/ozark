<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <link rel="stylesheet" type="text/css" href="${mvc.contextPath}/ozark.css"/>
    <title>Form Binding Error</title>
</head>
<body>
    <h1>Binding Error</h1>
    <p>Param: ${error.property}</p>
    <p>Message: ${error.message}</p>
</html>
