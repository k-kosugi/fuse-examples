package com.redhat.japan;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CSVtoXML extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("file://./temp?fileName=sample.csv&noop=true")
                // backup
                .to("file://./temp2?fileName=${file:name}.bak")
                .unmarshal("csv")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        List<Author> authors = new ArrayList<>();
                        List<List<String>> data = (List<List<String>>) exchange.getIn().getBody();
                        data.forEach(x -> {
                            String value = x.get(0).trim();
                            int id = Integer.parseInt(x.get(1).trim());
                            String etc = x.get(2).trim();
                            authors.add(new Author(value, id, etc));
                        });

                        exchange.getIn().setBody(authors);
                    }
                })
                .log("before ------- ${body}")
                .marshal().jacksonxml()
                // content filter
                .choice()
                .when(xpath("//id='80'"))
                    .log("after ------- ${body}")
                    .to("file://./temp2?fileName=${file:name}.when")
                .otherwise()
                    .log("after ------- ${body}")
                    .to("file://./temp2?fileName=${file:name}.otherwise");
    }

}
