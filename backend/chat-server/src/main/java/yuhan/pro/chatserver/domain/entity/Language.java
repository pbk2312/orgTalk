package yuhan.pro.chatserver.domain.entity;


import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.INVALID_LANGUAGE_VALUE;

import lombok.AllArgsConstructor;
import lombok.Getter;
import yuhan.pro.chatserver.sharedkernel.exception.CustomException;

@Getter
@AllArgsConstructor
public enum Language {
  JAVASCRIPT("javascript"),
  PYTHON("python"),
  JAVA("java"),
  CPP("cpp"),
  C("c"),
  CSHARP("csharp"),
  PHP("php"),
  RUBY("ruby"),
  GO("go"),
  RUST("rust"),
  TYPESCRIPT("typescript"),
  HTML("html"),
  CSS("css"),
  SQL("sql"),
  BASH("bash"),
  JSON("json"),
  XML("xml"),
  YAML("yaml");

  private final String value;


  public static Language fromValue(String value) {
    for (Language lang : Language.values()) {
      if (lang.value.equalsIgnoreCase(value)) {
        return lang;
      }
    }
    throw new CustomException(INVALID_LANGUAGE_VALUE);
  }
}
