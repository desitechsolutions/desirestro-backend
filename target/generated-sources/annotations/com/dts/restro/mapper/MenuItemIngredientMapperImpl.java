package com.dts.restro.mapper;

import com.dts.restro.dto.menu.MenuItemIngredientDTO;
import com.dts.restro.entity.Ingredient;
import com.dts.restro.entity.MenuItemIngredient;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-15T10:42:15+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class MenuItemIngredientMapperImpl implements MenuItemIngredientMapper {

    @Override
    public MenuItemIngredientDTO toDTO(MenuItemIngredient entity) {
        if ( entity == null ) {
            return null;
        }

        MenuItemIngredientDTO menuItemIngredientDTO = new MenuItemIngredientDTO();

        menuItemIngredientDTO.setIngredientId( entityIngredientId( entity ) );
        menuItemIngredientDTO.setQuantityRequired( entity.getQuantityRequired() );

        return menuItemIngredientDTO;
    }

    @Override
    public MenuItemIngredient toEntity(MenuItemIngredientDTO dto) {
        if ( dto == null ) {
            return null;
        }

        MenuItemIngredient menuItemIngredient = new MenuItemIngredient();

        menuItemIngredient.setIngredient( menuItemIngredientDTOToIngredient( dto ) );
        menuItemIngredient.setQuantityRequired( dto.getQuantityRequired() );

        return menuItemIngredient;
    }

    @Override
    public List<MenuItemIngredientDTO> toDTOList(List<MenuItemIngredient> entities) {
        if ( entities == null ) {
            return null;
        }

        List<MenuItemIngredientDTO> list = new ArrayList<MenuItemIngredientDTO>( entities.size() );
        for ( MenuItemIngredient menuItemIngredient : entities ) {
            list.add( toDTO( menuItemIngredient ) );
        }

        return list;
    }

    @Override
    public List<MenuItemIngredient> toEntityList(List<MenuItemIngredientDTO> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<MenuItemIngredient> list = new ArrayList<MenuItemIngredient>( dtos.size() );
        for ( MenuItemIngredientDTO menuItemIngredientDTO : dtos ) {
            list.add( toEntity( menuItemIngredientDTO ) );
        }

        return list;
    }

    private Long entityIngredientId(MenuItemIngredient menuItemIngredient) {
        Ingredient ingredient = menuItemIngredient.getIngredient();
        if ( ingredient == null ) {
            return null;
        }
        return ingredient.getId();
    }

    protected Ingredient menuItemIngredientDTOToIngredient(MenuItemIngredientDTO menuItemIngredientDTO) {
        if ( menuItemIngredientDTO == null ) {
            return null;
        }

        Ingredient ingredient = new Ingredient();

        ingredient.setId( menuItemIngredientDTO.getIngredientId() );

        return ingredient;
    }
}
