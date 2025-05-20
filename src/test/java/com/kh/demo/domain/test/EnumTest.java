package com.kh.demo.domain.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/*
  enum : 1. 한정된 상수 객체를 멤버로 갖는 타입
         2. 각각의 상수 객체는 속성과 행위를 가질수 있다.
 */
@Slf4j
public class EnumTest {

  @Test
  void test1(){

    enum Day{
      SUNDAY,
      MONDAY,
      TUESDAY,
      WEDNESDAY,
      THURSDAY,
      FRIDAY,
      SATURDAY
    }

    Day day = Day.SUNDAY;
    log.info("day={}", day);

    for (Day d :Day.values() ) {
      log.info("day={}", d);
    }
  }

  @Test
  void test2() {
    enum Day {
      SUNDAY("Weekend"),
      MONDAY("Weekday"),
      TUESDAY("Weekday"),
      WEDNESDAY("Weekday"),
      THURSDAY("Weekday"),
      FRIDAY("Weekday"),
      SATURDAY("Weekend");

      private String type;
//
      // 생성자
      Day(String type) {
        this.type = type;
      }
//
      // 메서드
      public String getType() {
        return type;
      }
    }
  }

  @Test
  void test3(){
    enum Fruit {
      APPLE(8, "한국"),
      BANANA(9, "미국"),
      ORANGE(10, "호주");

      //속성
      private final int sweetness; //당도
      private final String origin; //원산지

      //생성
      Fruit(int sweetness, String origin) {
        this.sweetness = sweetness;
        this.origin = origin;
      }
      //getter
      public int getSweetness(){
        return sweetness;
      }
      public String getOrigin(){
        return origin;
      }

      @Override
      public String toString(){
        return String.format("과일: %s, 당도: %d, 원산지 %s", name(), sweetness, origin);
      }
    }

    Fruit fruit = Fruit.APPLE;
    log.info("fruit={}", fruit);
    // 특정과일의 속성에 접근
    log.info("사과의 당도={}", fruit.getSweetness());
    log.info("사과의 원산지={}", fruit.getOrigin());

    log.info(fruit.name());

    //모든 과일정보 출력
    for (Fruit f : Fruit.values()) {
      log.info("과일={}",f);
    }

  }
}
