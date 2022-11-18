package com.vin.test;

///usr/bin/env jbang "$0" "$@" ; exit $?
/*
These commented lines make the class executable if you have jbang installed by making the file
executable (eg: chmod +x ./PerformanceTest.java) and just executing it with ./PerformanceTest.java
*/
//DEPS org.assertj:assertj-core:3.23.1
//DEPS org.junit.jupiter:junit-jupiter-engine:5.9.0
//DEPS org.junit.platform:junit-platform-launcher:1.9.0
//DEPS us.abstracta.jmeter:jmeter-java-dsl-wrapper:1.2
//DEPS us.abstracta.jmeter:jmeter-java-dsl:1.2

import org.apache.jmeter.extractor.gui.XPathExtractorGui;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.sampler.DebugSampler;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class PerformanceTest {

    @Test
    public void test() throws IOException {
        TestPlanStats stats = testPlan(
                httpDefaults()
                        .url("https://bank-of-anthos.xyz"),
                httpHeaders(),
                threadGroup(1, 1,
                        httpSampler("got_login_page", "/login"),
                        httpSampler("to_login", "/login")
                                .method(HTTPConstants.POST)
                                .rawParam("username", "testuser")
                                .rawParam("password", "password")
                                .children(
                                        testElement(new XPathExtractorGui())
                                                .prop("XPathExtractor.default", "no_result_found")
                                                .prop("XPathExtractor.refname", "extract_lst_txn_amt")
                                                .prop("XPathExtractor.xpathQuery", "//tbody//td[@class='transaction-amount transaction-amount-debit']")
                                                .prop("XPathExtractor.tolerant", true)
                                ),
                        jsr223Sampler("JSR223 Sampler- lidt initialization", "List<Double> amtLst = new ArrayList<Double>();\n"
                                + "\n"
                                + "vars.putObject(\"amtLst\",amtLst);"),
                        forEachController("ForEach Controller ", "extract_lst_txn_amt", "each_txn_amt",
                                jsr223Sampler("JSR223 Sampler - ${each_txn_amt}", "var lst = vars.get(\"each_txn_amt\")\n"
                                        + "\n"
                                        + "\n"
                                        + "t = vars.getObject(\"amtLst\");\n"
                                        + "var rmDollar = lst.replaceAll('\\\\$', '');\n"
                                        + "var rmComma = rmDollar.replaceAll('\\\\,', '');\n"
                                        + "\n"
                                        + "var d = Double.parseDouble(rmComma);\n"
                                        + "\n"
                                        + "t.add(d);\n"
                                        + "\n"
                                        + "vars.putObject(\"t\",t);\n"
                                        + "\n"
                                        + "\n"
                                        + "")
                        ),
                        testElement("Debug Sampler", new DebugSampler())
                                .prop("displayJMeterVariables", true),
                        jsr223Sampler("JSR223 Sampler ${t}", "log.info(\"hello\")\n"
                                + ""),
                        resultsTreeVisualizer()
                )
        )
                .run();
        assertThat(stats.overall().errorsCount()).isEqualTo(0);
    }

//    @Test
//    public void firstTest() throws IOException {
//        TestPlanStats stats =
//                testPlan(
//                        threadGroup(1,2,
//                                httpSampler("http://opencart.abstracta.us")
//                        ),
//                        htmlReporter("report/test_report")
//                ).run();
//
//        assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
//
//    }

}
