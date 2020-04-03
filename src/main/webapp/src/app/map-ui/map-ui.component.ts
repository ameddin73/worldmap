import {Component, Directive, OnInit, ViewChild} from '@angular/core';
import {Program} from "../models/program.model";
import {ProgramService} from "../shared/program/program.service";
import {FormBuilder} from "@angular/forms";

@Component({
  selector: 'app-map-ui',
  templateUrl: './map-ui.component.html',
  styleUrls: ['./map-ui.component.css']
})
export class MapUiComponent implements OnInit {
  programs: Array<Program>;

  localDateObject: LocalDateObject;
  rangeSliderValues = {range_start: null, range_end: null};
  maxValue = (new Date()).getFullYear().valueOf();
  minValue = 1900;

  form = this.formBuilder.group({ 'single': [ 10 ] });

  constructor(private programService: ProgramService,
              private formBuilder: FormBuilder) { }

  ngOnInit() {
    this.programService.getAll().subscribe((data) => {
      this.programs = data._embedded.programs;
      this.programs.sort(((a, b) => {return (a.startDate<b.startDate?1:-1)}));
      var oldest = this.programs[0].startDate;
      var newest = this.programs[this.programs.length - 1].startDate;
      this.localDateObject = {
        oldest: oldest,
        newest: newest,
        date: new Date(),
        startDate: oldest,
        endDate: newest
      };
    });
  }

}

export interface LocalDateObject {
  oldest: Date,
  newest: Date,
  date: Date,
  startDate: Date,
  endDate: Date
};
