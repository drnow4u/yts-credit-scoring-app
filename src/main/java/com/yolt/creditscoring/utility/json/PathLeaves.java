package com.yolt.creditscoring.utility.json;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class PathLeaves implements Function<String, List<String>> {
    @Override
    public List<String> apply(String json) {
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);

        List<String> jsonPaths = List.of("$['iban']",
                "$['initialBalance']",
                "$['newestTransactionDate']",
                "$['oldestTransactionDate']",
                "$['creditLimit']",
                "$['transactionsSize']");
        return jsonPaths.stream()
                .filter(path -> {
                        Object leaf = JsonPath.read(document, path);
                        return !Objects.isNull(leaf);
                })
                .toList();
    }
}
