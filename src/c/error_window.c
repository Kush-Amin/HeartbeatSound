#include <pebble.h>
#include "error_window.h"

Window *errorWindow;
MenuLayer *errorMenuLayer;

uint16_t men_get_num_sections_callback(MenuLayer *menu_layer, void *data) {
 return 2;
}

uint16_t men_get_num_rows_callback(MenuLayer *menu_layer, uint16_t section_index, void *data) {
  switch(section_index){
    case 0:
      return 4;
    case 1:
      return 1;
     case 2:
      return 1;
     case 4:
      return 1;
    default:
      return 0;
  
  }
}

int16_t men_get_header_height_callback(MenuLayer *menu_layer, uint16_t section_index, void *data) {
    return MENU_CELL_BASIC_HEADER_HEIGHT;
}

void men_draw_header_callback(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
    switch(section_index){
      case 0:
        menu_cell_basic_header_draw(ctx, cell_layer,"How you feeling?");
        break;
      case 1:
       menu_cell_basic_header_draw(ctx, cell_layer, NULL);
        break;

    }
  
}

void men_draw_row_callback(GContext* ctx, const Layer *cell_layer, MenuIndex *cell_index, void *data) {
    switch(cell_index->section){
      case 0:
        switch(cell_index->row) {
          case 0:
            menu_cell_basic_draw(ctx, cell_layer, "Working Out", NULL, NULL); 
           break;
          case 1:
            menu_cell_basic_draw(ctx, cell_layer, "Sleeping", NULL, NULL); 
           break;
           case 2:
            menu_cell_basic_draw(ctx, cell_layer, "Working", NULL, NULL); 
           break;
          case 3:
            menu_cell_basic_draw(ctx, cell_layer, "Not Much", NULL, NULL);
           break;
        }
         break;
      case 1:
         menu_cell_basic_draw(ctx, cell_layer, "Search Playlist", NULL, NULL);
        break;
}
}
void men_select_callback(MenuLayer *menu_layer, MenuIndex *cell_index, void *data) {

  DictionaryIterator *iter;
   
  switch(cell_index->section){
    
      case 0:
        switch(cell_index->row) {
    case 0:

       app_message_outbox_begin(&iter);
      if(iter == NULL) {
      APP_LOG(APP_LOG_LEVEL_ERROR, "Iter is null! Refusing to send");
     
      dict_write_uint16(iter, 10000, 713);
      dict_write_end(iter);
      app_message_outbox_send();
      break;
      }
    case 1:

      app_message_outbox_begin(&iter);
      if(iter == NULL) {
      APP_LOG(APP_LOG_LEVEL_ERROR, "Iter is null! Refusing to send");
      dict_write_uint16(iter, 10001, 713);
      dict_write_end(iter);
      app_message_outbox_send();
      break;
      }
    case 2:
      app_message_outbox_begin(&iter);
      if(iter == NULL) {
      APP_LOG(APP_LOG_LEVEL_ERROR, "Iter is null! Refusing to send");
        dict_write_uint16(iter, 10002, 713);
      dict_write_end(iter);
      app_message_outbox_send();
      break;
      }
     
    case 3:
     
     
    break;
}
  }
}
void process_tuple(Tuple *t){
    int key = t->key;
    int value = t->value->int32;
    APP_LOG(APP_LOG_LEVEL_INFO, "Got key %d with value %d", key, value);
}

void messag_inbox(DictionaryIterator *iter, void *context){
    Tuple *t = dict_read_first(iter);
    if(t){
        process_tuple(t);
    }
    while(t != NULL){
        t = dict_read_next(iter);
        if(t){
            process_tuple(t);
        }
    }
}

void message_inbox_dropped(AppMessageResult reason, void *context){
  APP_LOG(APP_LOG_LEVEL_INFO, "Message dropped, reason %d.", reason);
}
//void menu_layer_set_normal_colors(MenuLayer * menu_layer, GColor background, GColor foreground) {
    
  
//}

void setup_menu_layer(Window *window) {
	Layer *window_layer = window_get_root_layer(window);
    
    errorMenuLayer = menu_layer_create(GRect(0, 0, 144, 168));
  //  menu_layer_set_normal_colors(errorMenuLayer, GColorRed, GColorWhite); 
    menu_layer_set_callbacks(errorMenuLayer, NULL, (MenuLayerCallbacks){
        .get_num_sections = men_get_num_sections_callback,
        .get_num_rows = men_get_num_rows_callback,
        .get_header_height = men_get_header_height_callback,
        .draw_header = men_draw_header_callback,
        .draw_row = men_draw_row_callback,
        .select_click = men_select_callback,
        
    });
     
    menu_layer_set_click_config_onto_window(errorMenuLayer, window);

    layer_add_child(window_layer, menu_layer_get_layer(errorMenuLayer));
}

void error_window_load(Window *window){
 setup_menu_layer(window);
  
 app_message_register_inbox_received(messag_inbox);
  app_message_register_inbox_dropped(message_inbox_dropped);
 app_message_open(256, 256);
  
}

void error_window_unload(Window *window){

menu_layer_destroy(errorMenuLayer);
}


void error_window_create() {
  errorWindow = window_create();
  window_set_window_handlers(errorWindow, (WindowHandlers)
  {
    .load = error_window_load,
    .unload = error_window_unload
  } );
}

void error_window_destroy() {
  window_destroy(errorWindow);
}

Window *error_window_get_window() {
  return errorWindow;
}
