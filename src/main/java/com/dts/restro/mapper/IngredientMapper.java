package com.dts.restro.mapper;

import com.dts.restro.dto.IngredientDTO;
import com.dts.restro.entity.Ingredient;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IngredientMapper {

    IngredientDTO toDto(Ingredient entity);

    Ingredient toEntity(IngredientDTO dto);
}
