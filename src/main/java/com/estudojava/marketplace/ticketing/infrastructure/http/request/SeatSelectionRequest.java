package com.estudojava.marketplace.ticketing.infrastructure.http.request;

import com.estudojava.marketplace.ticketing.domain.SeatId;

public record SeatSelectionRequest (String id){
    public SeatId toInput(){
        return new SeatId(id);
    }
}
