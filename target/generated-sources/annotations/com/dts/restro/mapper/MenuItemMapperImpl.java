package com.dts.restro.mapper;

import com.dts.restro.dto.menu.MenuItemDTO;
import com.dts.restro.entity.Category;
import com.dts.restro.entity.MenuItem;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-15T10:42:15+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class MenuItemMapperImpl implements MenuItemMapper {

    @Autowired
    private MenuItemIngredientMapper menuItemIngredientMapper;

    @Override
    public MenuItemDTO toDTO(MenuItem entity) {
        if ( entity == null ) {
            return null;
        }

        MenuItemDTO menuItemDTO = new MenuItemDTO();

        menuItemDTO.setCategoryId( entityCategoryId( entity ) );
        menuItemDTO.setCategoryName( entityCategoryName( entity ) );
        menuItemDTO.setId( entity.getId() );
        menuItemDTO.setName( entity.getName() );
        menuItemDTO.setDescription( entity.getDescription() );
        menuItemDTO.setPrice( entity.getPrice() );
        menuItemDTO.setVeg( entity.isVeg() );
        menuItemDTO.setAvailable( entity.isAvailable() );
        menuItemDTO.setIngredients( menuItemIngredientMapper.toDTOList( entity.getIngredients() ) );

        return menuItemDTO;
    }

    @Override
    public MenuItem toEntity(MenuItemDTO dto) {
        if ( dto == null ) {
            return null;
        }

        MenuItem menuItem = new MenuItem();

        menuItem.setCategory( menuItemDTOToCategory( dto ) );
        menuItem.setId( dto.getId() );
        menuItem.setName( dto.getName() );
        menuItem.setDescription( dto.getDescription() );
        menuItem.setPrice( dto.getPrice() );
        menuItem.setVeg( dto.isVeg() );
        menuItem.setAvailable( dto.isAvailable() );
        menuItem.setIngredients( menuItemIngredientMapper.toEntityList( dto.getIngredients() ) );

        return menuItem;
    }

    @Override
    public List<MenuItemDTO> toDTOList(List<MenuItem> entities) {
        if ( entities == null ) {
            return null;
        }

        List<MenuItemDTO> list = new ArrayList<MenuItemDTO>( entities.size() );
        for ( MenuItem menuItem : entities ) {
            list.add( toDTO( menuItem ) );
        }

        return list;
    }

    private Long entityCategoryId(MenuItem menuItem) {
        Category category = menuItem.getCategory();
        if ( category == null ) {
            return null;
        }
        return category.getId();
    }

    private String entityCategoryName(MenuItem menuItem) {
        Category category = menuItem.getCategory();
        if ( category == null ) {
            return null;
        }
        return category.getName();
    }

    protected Category menuItemDTOToCategory(MenuItemDTO menuItemDTO) {
        if ( menuItemDTO == null ) {
            return null;
        }

        Category category = new Category();

        category.setId( menuItemDTO.getCategoryId() );

        return category;
    }
}
