import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CandidateService } from '../../services/candidate.service';
import { Candidate } from '../../models/candidate.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {

  candidate: Candidate = {
    firstName: '',
    lastName: '',
    employeeId: '',
    dob: '',
    email: '',
    role: '',
    password: '',
    jobId: 0
  };

  confirmPassword = '';
  showPassword = false;
  showConfirmPassword = false;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';

  // Typeahead Job Role properties with expanded IT roles
  availableRoles: string[] = [
    'Software Engineer',
    'Software Developer',
    'Java Developer',
    'Java Backend Developer',
    'Java Full Stack Developer',
    'Python Developer',
    '.NET Developer',
    'C# Developer',
    'Angular Developer',
    'React Developer',
    'Frontend Developer',
    'Backend Developer',
    'Full Stack Developer',
    'QA Engineer',
    'Test Engineer',
    'Automation Test Engineer',
    'DevOps Engineer',
    'Cloud Engineer',
    'Data Analyst',
    'Data Engineer',
    'Data Scientist',
    'Database Administrator',
    'UI/UX Developer',
    'Business Analyst',
    'System Engineer',
    'Network Engineer',
    'Cyber Security Engineer',
    'Machine Learning Engineer',
    'AI Engineer',
    'Technical Support Engineer',
    'Project Engineer',
    'Employee (General)'
  ];

  roleInput = '';
  filteredRoles: string[] = [];
  showRoleDropdown = false;
  selectedRoleIndex = -1;

  constructor(
    private candidateService: CandidateService,
    private router: Router
  ) {}

  get minDobDate(): string {
    const today = new Date();
    const minDate = new Date(today.getFullYear() - 80, today.getMonth(), today.getDate());
    return minDate.toISOString().split('T')[0];
  }

  get maxDobDate(): string {
    const today = new Date();
    const maxDate = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());
    return maxDate.toISOString().split('T')[0];
  }

  // Typeahead & Dropdown Role Handlers
  openRoleDropdown(): void {
    const val = (this.roleInput || '').trim().toLowerCase();
    this.filterRoles(val);
    this.showRoleDropdown = true;
    this.selectedRoleIndex = -1;
  }

  toggleRoleDropdown(event: Event): void {
    event.stopPropagation();
    if (this.showRoleDropdown) {
      this.showRoleDropdown = false;
    } else {
      this.openRoleDropdown();
    }
  }

  filterRoles(val: string): void {
    if (!val) {
      this.filteredRoles = [...this.availableRoles];
      return;
    }
    const prefixMatches = this.availableRoles.filter(r => r.toLowerCase().startsWith(val));
    const otherMatches = this.availableRoles.filter(r => !r.toLowerCase().startsWith(val) && r.toLowerCase().includes(val));
    this.filteredRoles = [...prefixMatches, ...otherMatches];

    if (this.filteredRoles.length === 0) {
      this.filteredRoles = ['Employee (General)'];
    }
  }

  onRoleInput(): void {
    this.selectedRoleIndex = -1;
    const val = (this.roleInput || '').trim().toLowerCase();
    this.filterRoles(val);
    this.showRoleDropdown = true;

    const exactMatch = this.availableRoles.find(r => r.toLowerCase() === val);
    if (exactMatch) {
      this.candidate.role = exactMatch;
    } else {
      this.candidate.role = '';
    }
  }

  selectRole(role: string): void {
    this.roleInput = role;
    this.candidate.role = role;
    this.showRoleDropdown = false;
    this.filteredRoles = [];
    this.selectedRoleIndex = -1;
  }

  onRoleKeyDown(event: KeyboardEvent): void {
    if (!this.showRoleDropdown || this.filteredRoles.length === 0) {
      if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
        this.openRoleDropdown();
      }
      return;
    }

    if (event.key === 'ArrowDown') {
      event.preventDefault();
      this.selectedRoleIndex = (this.selectedRoleIndex + 1) % this.filteredRoles.length;
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      this.selectedRoleIndex = (this.selectedRoleIndex - 1 + this.filteredRoles.length) % this.filteredRoles.length;
    } else if (event.key === 'Enter') {
      if (this.selectedRoleIndex >= 0 && this.selectedRoleIndex < this.filteredRoles.length) {
        event.preventDefault();
        this.selectRole(this.filteredRoles[this.selectedRoleIndex]);
      }
    } else if (event.key === 'Escape') {
      this.showRoleDropdown = false;
      this.selectedRoleIndex = -1;
    }
  }

  onRoleBlur(): void {
    setTimeout(() => {
      this.showRoleDropdown = false;
      const exact = this.availableRoles.find(r => r.toLowerCase() === this.roleInput.trim().toLowerCase());
      if (exact) {
        this.roleInput = exact;
        this.candidate.role = exact;
      }
    }, 200);
  }

  // Duplicate Error Helpers
  isDuplicateEmpIdError(): boolean {
    const msg = (this.errorMessage || '').toLowerCase();
    return msg.includes('employee id') && !msg.includes('email') && (msg.includes('already exist') || msg.includes('registered'));
  }

  isDuplicateEmailError(): boolean {
    const msg = (this.errorMessage || '').toLowerCase();
    return msg.includes('email') && !msg.includes('employee id') && (msg.includes('already exist') || msg.includes('registered'));
  }

  isDuplicateCompositeError(): boolean {
    const msg = (this.errorMessage || '').toLowerCase();
    return msg.includes('employee id') && msg.includes('email');
  }

  // Password Policy Checks
  hasMinLength(): boolean {
    return (this.candidate.password || '').length >= 8;
  }

  hasUppercase(): boolean {
    return /[A-Z]/.test(this.candidate.password || '');
  }

  hasLowercase(): boolean {
    return /[a-z]/.test(this.candidate.password || '');
  }

  hasNumber(): boolean {
    return /[0-9]/.test(this.candidate.password || '');
  }

  hasSpecial(): boolean {
    return /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(this.candidate.password || '');
  }

  isPasswordValid(): boolean {
    return this.hasMinLength() && this.hasUppercase() && this.hasLowercase() && this.hasNumber() && this.hasSpecial();
  }

  doPasswordsMatch(): boolean {
    return !!this.confirmPassword && this.candidate.password === this.confirmPassword;
  }

  // Field Validation Helpers
  hasFirstNameDigits(): boolean {
    return /\d/.test(this.candidate.firstName || '');
  }

  isFirstNameAdmin(): boolean {
    return (this.candidate.firstName || '').trim().toLowerCase() === 'admin';
  }

  isFirstNameValid(): boolean {
    const fn = (this.candidate.firstName || '').trim();
    if (!fn) return false;
    if (this.hasFirstNameDigits()) return false;
    if (this.isFirstNameAdmin()) return false;
    return true;
  }

  hasLastNameDigits(): boolean {
    return /\d/.test(this.candidate.lastName || '');
  }

  isLastNameValid(): boolean {
    const ln = (this.candidate.lastName || '').trim();
    if (!ln) return true; // Optional!
    return !this.hasLastNameDigits();
  }

  isAgeValid(): boolean {
    if (!this.candidate.dob) return false;
    const dobDate = new Date(this.candidate.dob);
    const today = new Date();
    if (isNaN(dobDate.getTime()) || dobDate > today) return false;
    let age = today.getFullYear() - dobDate.getFullYear();
    const m = today.getMonth() - dobDate.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < dobDate.getDate())) {
      age--;
    }
    return age >= 18 && age <= 80;
  }

  isAdminEmail(): boolean {
    return (this.candidate.email || '').trim().toLowerCase() === 'admin@company.com';
  }

  isEmailValid(): boolean {
    const em = (this.candidate.email || '').trim().toLowerCase();
    if (!em) return false;
    if (this.isAdminEmail()) return false;
    return /^[^@\s]+@company\.com$/.test(em);
  }

  isEmployeeIdValid(): boolean {
    return !!(this.candidate.employeeId || '').trim();
  }

  isRoleValid(): boolean {
    return !!this.candidate.role && this.availableRoles.includes(this.candidate.role);
  }

  isFormValid(): boolean {
    return this.isFirstNameValid() &&
           this.isLastNameValid() &&
           this.isEmployeeIdValid() &&
           this.isAgeValid() &&
           this.isEmailValid() &&
           this.isRoleValid() &&
           this.isPasswordValid() &&
           this.doPasswordsMatch();
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.isFormValid()) {
      this.errorMessage = 'Please fix all validation errors before registering.';
      return;
    }

    this.isSubmitting = true;

    this.candidateService.registerEmployee(this.candidate).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.successMessage = 'Registration successful! Redirecting to login...';
        setTimeout(() => {
          this.router.navigate(['/login'], { queryParams: { registered: 'true' } });
        }, 1500);
      },
      error: (err) => {
        this.isSubmitting = false;
        if (err.status === 409) {
          this.errorMessage = err.error?.message || 'An employee with this Employee ID and email already exists.';
        } else if (err.status === 500) {
          this.errorMessage = 'Unable to complete registration. Please try again.';
        } else {
          this.errorMessage = err.error?.message || 'Registration failed. Please check your details and try again.';
        }
      }
    });
  }
}
