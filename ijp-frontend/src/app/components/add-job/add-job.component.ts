import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { JobService } from '../../services/job.service';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import { JobPosting } from '../../models/job.model';
import { Designation } from '../../models/designation.model';

@Component({
  selector: 'app-add-job',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './add-job.component.html',
  styleUrls: ['./add-job.component.css']
})
export class AddJobComponent implements OnInit {

  job: JobPosting = {
    jobId: 'JOB101',
    description: '',
    designation: '',
    location: '',
    skillSet: '',
    experience: '',
    salaryMin: 0,
    salaryMax: 0,
    status: 'OPEN'
  };

  activeDesignations: Designation[] = [];
  filteredDesignations: string[] = [];
  showDesignationDropdown = false;
  selectedDesignationIndex = -1;

  isSubmitting = false;
  errorMessage = '';

  constructor(
    private jobService: JobService,
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadNextJobCode();
    this.loadActiveDesignations();
  }

  loadNextJobCode(): void {
    this.jobService.getNextJobCode().subscribe({
      next: (res) => {
        if (res && res.jobCode) {
          this.job.jobId = res.jobCode;
        }
      },
      error: (err) => console.error('Failed to load next job code:', err)
    });
  }

  loadActiveDesignations(): void {
    this.adminService.getActiveDesignations().subscribe({
      next: (data) => {
        this.activeDesignations = data;
      },
      error: (err) => console.error('Failed to load active designations:', err)
    });
  }

  // Typeahead for Designation
  onDesignationInput(): void {
    this.selectedDesignationIndex = -1;
    const val = (this.job.designation || '').trim().toLowerCase();
    if (!val) {
      this.filteredDesignations = [];
      this.showDesignationDropdown = false;
      return;
    }

    const allNames = this.activeDesignations.map(d => d.name);
    const prefixMatches = allNames.filter(n => n.toLowerCase().startsWith(val));
    const otherMatches = allNames.filter(n => !n.toLowerCase().startsWith(val) && n.toLowerCase().includes(val));
    this.filteredDesignations = [...prefixMatches, ...otherMatches];
    this.showDesignationDropdown = this.filteredDesignations.length > 0;
  }

  selectDesignation(name: string): void {
    this.job.designation = name;
    this.showDesignationDropdown = false;
    this.filteredDesignations = [];
    this.selectedDesignationIndex = -1;
  }

  onDesignationKeyDown(event: KeyboardEvent): void {
    if (!this.showDesignationDropdown || this.filteredDesignations.length === 0) return;

    if (event.key === 'ArrowDown') {
      event.preventDefault();
      this.selectedDesignationIndex = (this.selectedDesignationIndex + 1) % this.filteredDesignations.length;
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      this.selectedDesignationIndex = (this.selectedDesignationIndex - 1 + this.filteredDesignations.length) % this.filteredDesignations.length;
    } else if (event.key === 'Enter') {
      if (this.selectedDesignationIndex >= 0 && this.selectedDesignationIndex < this.filteredDesignations.length) {
        event.preventDefault();
        this.selectDesignation(this.filteredDesignations[this.selectedDesignationIndex]);
      }
    } else if (event.key === 'Escape') {
      this.showDesignationDropdown = false;
      this.selectedDesignationIndex = -1;
    }
  }

  onDesignationBlur(): void {
    setTimeout(() => {
      this.showDesignationDropdown = false;
    }, 200);
  }

  // Validation Checks
  isDesignationValid(): boolean {
    return !!(this.job.designation || '').trim();
  }

  isDescriptionValid(): boolean {
    return !!(this.job.description || '').trim();
  }

  isLocationValid(): boolean {
    return !!(this.job.location || '').trim();
  }

  isExperienceValid(): boolean {
    return !!(this.job.experience || '').trim();
  }

  isSkillSetValid(): boolean {
    return !!(this.job.skillSet || '').trim();
  }

  isSalaryMinValid(): boolean {
    return this.job.salaryMin !== null && this.job.salaryMin !== undefined && this.job.salaryMin > 0;
  }

  isSalaryMaxValid(): boolean {
    return this.job.salaryMax !== null && this.job.salaryMax !== undefined && this.job.salaryMax > 0 && this.job.salaryMax >= (this.job.salaryMin || 0);
  }

  isFormValid(): boolean {
    return this.isDesignationValid() &&
           this.isDescriptionValid() &&
           this.isLocationValid() &&
           this.isExperienceValid() &&
           this.isSkillSetValid() &&
           this.isSalaryMinValid() &&
           this.isSalaryMaxValid();
  }

  onSubmit(): void {
    this.errorMessage = '';

    if (!this.isFormValid()) {
      this.errorMessage = 'Please complete all required fields before publishing.';
      return;
    }

    this.isSubmitting = true;

    this.jobService.createJob(this.job).subscribe({
      next: (created) => {
        this.isSubmitting = false;
        this.router.navigate(['/admin-dashboard']);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message || 'Failed to create job posting. Please try again.';
        console.error(err);
      }
    });
  }
}
