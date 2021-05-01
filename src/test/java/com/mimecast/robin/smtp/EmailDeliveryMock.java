package com.mimecast.robin.smtp;

import com.mimecast.robin.smtp.connection.Connection;

public class EmailDeliveryMock extends EmailDelivery {

    public EmailDeliveryMock(Connection connection) {
        super(connection.getSession());
        this.connection = connection;
    }
}
