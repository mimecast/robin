package com.mimecast.robin.assertion.mta.client;

import org.json.JSONArray;

public class LogsClientMock implements LogsClient {

    public LogsClientMock() {}

    @Override
    public void setServer(String server) {}

    @Override
    public JSONArray getLogs(String query) {
        JSONArray arr = new JSONArray();
        arr.put("INFO |0810-110152743|SmtpThread-30307|smtp.Receipt|dSuG02ERM-OO1-R2Eawg9A|||||Accepted connection from 8.8.8.8:7575");
        arr.put("INFO |0810-110152748|SmtpThread-30307|smtp.Receipt|dSuG02ERM-OO1-R2Eawg9A|||||> 220 stark.com ESMTP ; Fri, 10 Aug 2018 11:01:52 +0100");
        arr.put("INFO |0810-110155304|SmtpThread-30307|smtp.Receipt|dSuG02ERM-OO1-R2Eawg9A|||||< EHLO Tony");
        arr.put("INFO |0810-110155304|SmtpThread-30307|smtp.Receipt|dSuG02ERM-OO1-R2Eawg9A|||||> 250-example.com Hello [8.8.8.8]");
        arr.put("INFO |0810-110155304|SmtpThread-30307|smtp.Receipt|dSuG02ERM-OO1-R2Eawg9A|||||> 250-AUTH PLAIN LOGIN DIGEST-MD5");
        arr.put("INFO |0810-110155304|SmtpThread-30307|smtp.Receipt|dSuG02ERM-OO1-R2Eawg9A|||||> 250-STARTTLS");
        arr.put("INFO |0810-110155304|SmtpThread-30307|smtp.Receipt|dSuG02ERM-OO1-R2Eawg9A|||||> 250 HELP");
        arr.put("INFO |0810-110200421|SmtpThread-30307|smtp.Receipt|7qGJZ4oRNkWJPsu_7ug1nw|||||< AUTH LOGIN");
        arr.put("INFO |0810-110200421|SmtpThread-30307|smtp.Auth|7qGJZ4oRNkWJPsu_7ug1nw|||||> 334 VXNlcm5hbWU6");
        arr.put("INFO |0810-110202370|SmtpThread-30307|smtp.Auth|7qGJZ4oRNkWJPsu_7ug1nw|||||< dG9ueUBzdGFyay5jb20=");
        arr.put("INFO |0810-110202370|SmtpThread-30307|smtp.Auth|7qGJZ4oRNkWJPsu_7ug1nw|||||> 334 UGFzc3dvcmQ6");
        arr.put("INFO |0810-110203459|SmtpThread-30307|smtp.Auth|7qGJZ4oRNkWJPsu_7ug1nw|||||< ****");
        arr.put("INFO |0810-110203459|SmtpThread-30307|logging.MapReduce|7qGJZ4oRNkWJPsu_7ug1nw|||||MAPREDUCE:MTAAUTH|User=tony@stark.com|Pass=false|Ehlo=Tony|IP=8.8.8.8|Time=0|AuthType=LOGIN");
        arr.put("INFO |0810-110203459|SmtpThread-30307|smtp.Receipt|7qGJZ4oRNkWJPsu_7ug1nw|||||> 535 Incorrect authentication data - https://community.mimecast.com/docs/DOC-1369#535 [7qGJZ4oRNkWJPsu_7ug1nw.lh1]");
        arr.put("INFO |0810-110203462|SmtpThread-30307|logging.MapReduce|7qGJZ4oRNkWJPsu_7ug1nw|||||MAPREDUCE:MTACONNSUMMARY|IP=8.8.8.8");
        arr.put("INFO |0810-110203462|SmtpThread-30307|smtp.Receipt|7qGJZ4oRNkWJPsu_7ug1nw|||||Socket closed.");

        return arr;
    }
}
