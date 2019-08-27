import {Component, Input, OnInit} from '@angular/core';
import {MouseEvent} from "@agm/core";
import {Program} from "../models/program.model";
import {ProgramService} from "../shared/program/program.service";

@Component({
  selector: 'app-google-map',
  templateUrl: './google-map.component.html',
  styleUrls: ['./google-map.component.css']
})
export class GoogleMapComponent implements OnInit{
  programs: Array<Program>;
  @Input() date;

  // google maps zoom level
  zoom: number = 8;

  // initial center position for the map
  lat: number = 39.9526;
  lng: number = -75.1652;

  //markers
  markers: marker[];

  constructor(private programService: ProgramService) {}

  ngOnInit() {
    this.programService.getAll().subscribe(data => {
      this.programs = data._embedded.programs;
      this.programs.sort(((a, b) => {return (a.startDate<b.startDate?1:-1)}));
      this.markers = this.programs.map((program) => {
        return {
          lat: program.latitude,
          lng: program.longitude,
          label: program.name,
          city: program.city
        };
      });
    });
  }
}

// just an interface for type safety.
interface marker {
  lat: number;
  lng: number;
  label?: string;
  city: string;
}
