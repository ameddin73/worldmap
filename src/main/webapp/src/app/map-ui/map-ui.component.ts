import { Component, OnInit } from '@angular/core';
import {Program} from "../models/program.model";
import {ProgramService} from "../shared/program/program.service";
import {FormBuilder} from "@angular/forms";
import {NouisliderModule} from "ng2-nouislider";

@Component({
  selector: 'app-map-ui',
  templateUrl: './map-ui.component.html',
  styleUrls: ['./map-ui.component.css']
})
export class MapUiComponent implements OnInit {
  programs: Array<Program>;

  date: {
    oldest: Date,
    newest: Date,
    date: Date,
    startDate: Date,
    endDate: Date
  };

  form = this.formBuilder.group({ 'single': [ 10 ] });

  constructor(private programService: ProgramService,
              private formBuilder: FormBuilder) { }

  ngOnInit() {
    this.programService.getAll().subscribe(data => {
      this.programs = data._embedded.programs;
      this.programs.sort(((a, b) => {return (a.startDate<b.startDate?1:-1)}));
      this.date.oldest = this.programs[0].startDate;
      this.date.newest = this.programs[this.programs.length].startDate;
      this.date.startDate = this.date.oldest;
      this.date.endDate = this.date.newest;
    });
    this.date.date = new Date();
  }

}
