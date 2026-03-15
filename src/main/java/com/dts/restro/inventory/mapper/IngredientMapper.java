package com.dts.restro.inventory.mapper;

import com.dts.restro.inventory.dto.IngredientDTO;
import com.dts.restro.inventory.entity.Ingredient;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IngredientMapper {

    IngredientDTO toDto(Ingredient entity);

    Ingredient toEntity(IngredientDTO dto);
}
