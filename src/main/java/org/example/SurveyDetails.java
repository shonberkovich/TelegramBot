package org.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;
@AllArgsConstructor
@Getter
public class SurveyDetails {
    private String question;
    private List<String> options;

}
