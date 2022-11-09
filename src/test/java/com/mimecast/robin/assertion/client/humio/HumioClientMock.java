package com.mimecast.robin.assertion.client.humio;

import com.mimecast.robin.config.assertion.external.logs.LogsExternalClientConfig;
import com.mimecast.robin.smtp.connection.Connection;
import org.json.JSONArray;

class HumioClientMock extends HumioClient {

    /**
     * Constructs a new HumioClient instance.
     *
     * @param connection    Connection instance.
     * @param config        LogsExternalClient instance.
     * @param transactionId Transaction ID.
     */
    public HumioClientMock(Connection connection, LogsExternalClientConfig config, int transactionId) {
        super(connection, config, transactionId);
    }

    /**
     * Runs client.
     *
     * @return Server logs.
     */
    @Override
    public JSONArray run() {
        JSONArray array = new JSONArray();

        array.put("DEBUG|0810-110152743|SmtpThread-30307|smtp.Receipt|wiUcnEI38Tjdnqw984Gtjd|||||Closing Transmission Channel");
        array.put("INFO |0810-110152743|SmtpThread-30307|smtp.Receipt|dSuG02ERMxOO1cR2Eawg9A|||||Accepted connection from 8.8.8.8:7575");
        array.put("INFO |0810-110152743|SmtpThread-30307|smtp.Receipt|dSuG02ERMxOO1cR2Eawg9A|||||> EHLO example.com");
        array.put("INFO |0810-110152743|SmtpThread-30307|smtp.Receipt|dSuG02ERMxOO1cR2Eawg9A|||||> MAIL FROM:<tony@example.com> SIZE=294");
        array.put("INFO |0810-110152743|SmtpThread-30307|smtp.Receipt|dSuG02ERMxOO1cR2Eawg9A|||||> RCPT TO:<pepper@example.com>");
        array.put("INFO |0810-110152743|SmtpThread-30307|smtp.Receipt|dSuG02ERMxOO1cR2Eawg9A|||||> DATA");
        array.put("INFO |0810-110152743|SmtpThread-30307|smtp.Receipt|dSuG02ERMxOO1cR2Eawg9A|||||> .");
        array.put("INFO |0810-110152743|SmtpThread-30307|smtp.Receipt|dSuG02ERMxOO1cR2Eawg9A|||||> QUIT");
        array.put("DEBUG|0810-110152743|SmtpThread-30307|smtp.Receipt|dSuG02ERMxOO1cR2Eawg9A|||||Closing Transmission Channel");

        return array;
    }
}