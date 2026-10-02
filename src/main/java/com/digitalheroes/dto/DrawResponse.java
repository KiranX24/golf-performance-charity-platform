package com.digitalheroes.dto;
import com.digitalheroes.entity.*; import java.time.*; import java.util.*;
public record DrawResponse(Long id,LocalDate drawPeriod,ScoreMode mode,DrawStatus status,Short[] drawnNumbers,Instant simulatedAt,Instant publishedAt,int participantCount){}