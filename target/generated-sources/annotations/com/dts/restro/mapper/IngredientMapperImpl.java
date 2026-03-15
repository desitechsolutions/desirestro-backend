package com.dts.restro.mapper;

import com.dts.restro.dto.IngredientDTO;
import com.dts.restro.entity.Ingredient;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-15T10:42:15+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class IngredientMapperImpl implements IngredientMapper {

    @Override
    public IngredientDTO toDto(Ingredient entity) {
        if ( entity == null ) {
            return null;
        }

        IngredientDTO ingredientDTO = new IngredientDTO();

        ingredientDTO.setId( entity.getId() );
        ingredientDTO.setName( entity.getName() );
        ingredientDTO.setUnit( entity.getUnit() );
        ingredientDTO.setCurrentStock( entity.getCurrentStock() );
        ingredientDTO.setReorderLevel( entity.getReorderLevel() );

        return ingredientDTO;
    }

    @Override
    public Ingredient toEntity(IngredientDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Ingredient ingredient = new Ingredient();

        ingredient.setId( dto.getId() );
        ingredient.setName( dto.getName() );
        ingredient.setUnit( dto.getUnit() );
        ingredient.setCurrentStock( dto.getCurrentStock() );
        ingredient.setReorderLevel( dto.getReorderLevel() );

        return ingredient;
    }
}
