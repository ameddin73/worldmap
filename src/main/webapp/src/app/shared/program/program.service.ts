import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import '../constants';
import {BASE_URI, PROGRAMS_URI} from "../constants";

@Injectable({
  providedIn: 'root'
})
export class ProgramService {

  constructor(private http: HttpClient) { }

  getAll(): Observable<any> {
    return this.http.get(BASE_URI + PROGRAMS_URI);
  }
}
