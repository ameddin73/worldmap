import {Component, Input, OnInit} from '@angular/core';
import {Program} from "../models/program.model";
import {LocalDateObject} from '../map-ui/map-ui.component';

@Component({
  selector: 'app-google-map',
  templateUrl: './google-map.component.html',
  styleUrls: ['./google-map.component.css']
})
export class GoogleMapComponent implements OnInit{

  @Input() localDateObject: LocalDateObject;

  private _programs: Array<Program>;
  @Input()
  set programs(programs: Array<Program>) {
    this._programs = programs;
    this._programs.sort(((a, b) => {return (a.startDate<b.startDate?1:-1)}));
    this.markers = this._programs.map((program) => {
      return {
        lat: program.latitude,
        lng: program.longitude,
        label: program.name,
        city: program.city
      };
    });
  }

  // google maps zoom level
  zoom: number = 8;

  // initial center position for the map
  lat: number = 39.9526;
  lng: number = -75.1652;

  //markers
  markers: marker[];

  constructor() {}

  ngOnInit() {
  }
}

// just an interface for type safety.
interface marker {
  lat: number;
  lng: number;
  label?: string;
  city: string;
}
