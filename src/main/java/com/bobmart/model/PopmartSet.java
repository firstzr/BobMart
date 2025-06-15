package com.bobmart.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PopmartSet {

    private  final String preFix= "https://www.popmart.com/us/pop-now/set/";
    private String name;
    private String url;
    public PopmartSet(String name){
        this.name = name;
        this.url= preFix+name;
    }
} 