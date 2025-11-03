# Contributing to SplitIt 🤝

First off, thank you for considering contributing to SplitIt! It's people like you that make SplitIt such a great tool for splitting expenses and managing group finances.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Making Changes](#making-changes)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)

## 📜 Code of Conduct

This project and everyone participating in it is governed by our [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to [dhruv03.work@gmail.com](mailto:dhruv03.work@gmail.com).

## 🎯 How Can I Contribute?

### Reporting Bugs 🐛

Before creating bug reports, please check existing issues to avoid duplicates. When creating a bug report, include:

- **Clear title and description**
- **Steps to reproduce** the issue
- **Expected vs actual behavior**
- **Screenshots** if applicable
- **Environment details** (OS, Browser, Java version, etc.)

**Bug Report Template:**
```markdown
**Describe the bug**
A clear description of what the bug is.

**To Reproduce**
Steps to reproduce:
1. Go to '...'
2. Click on '...'
3. See error

**Expected behavior**
What you expected to happen.

**Screenshots**
If applicable, add screenshots.

**Environment:**
- OS: [e.g., macOS, Windows, Linux]
- Browser: [e.g., Chrome, Safari]
- Java Version: [e.g., 17]
- SplitIt Version: [e.g., 1.0.0]
```

### Suggesting Features 💡

We love new ideas! Before suggesting a feature:

1. **Check existing issues** to see if it's already proposed
2. **Describe the problem** your feature would solve
3. **Explain your solution** with examples
4. **Consider alternatives** you've thought of

**Feature Request Template:**
```markdown
**Is your feature request related to a problem?**
A clear description of the problem.

**Describe the solution you'd like**
What you want to happen.

**Describe alternatives you've considered**
Other solutions you've thought about.

**Additional context**
Any other context or screenshots.
```

### Contributing Code 💻

#### Good First Issues

Look for issues labeled:
- `good first issue` - Perfect for newcomers
- `help wanted` - We need community help
- `beginner-friendly` - Easy to get started

#### Areas We Need Help With

- **Frontend** (Angular/React)
  - UI/UX improvements
  - Responsive design enhancements
  - Accessibility features

- **Backend** (Spring Boot Microservices)
  - API endpoint improvements
  - Performance optimizations
  - New features implementation

- **Documentation**
  - API documentation
  - User guides
  - Code comments

- **Testing**
  - Unit tests
  - Integration tests
  - E2E tests

## 🚀 Development Setup

### Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **Node.js 18+** (for frontend)
- **Docker & Docker Compose**
- **Git**
- **PostgreSQL** (or use Docker)
- **MongoDB** (or use Docker)

### Quick Start

1. **Fork the repository**
   ```bash
   # Click the "Fork" button on GitHub
   ```

2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/SplitIt.git
   cd SplitIt
   ```

3. **Add upstream remote**
   ```bash
   git remote add upstream https://github.com/Dhruv1030/SplitIt.git
   ```

4. **Start infrastructure services**
   ```bash
   docker-compose up -d mongodb postgres kafka zookeeper zipkin
   ```

5. **Build all services**
   ```bash
   ./build.sh
   ```

6. **Run services**
   ```bash
   # Option 1: Using Docker
   docker-compose up -d

   # Option 2: Run locally
   cd user-service && mvn spring-boot:run
   cd group-service && mvn spring-boot:run
   # ... repeat for other services
   ```

7. **Verify services are running**
   ```bash
   curl http://localhost:8761  # Eureka Discovery Server
   curl http://localhost:8080/actuator/health  # API Gateway
   ```

### Development URLs

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **Zipkin Tracing**: http://localhost:9411
- **User Service**: http://localhost:8081
- **Group Service**: http://localhost:8082
- **Expense Service**: http://localhost:8083
- **Settlement Service**: http://localhost:8084

## 📁 Project Structure

```
SplitIt/
├── api-gateway/              # Spring Cloud Gateway
├── discovery-server/         # Eureka Service Discovery
├── user-service/            # User management & authentication
├── group-service/           # Group management
├── expense-service/         # Expense tracking & splitting
├── settlement-service/      # Settlement calculations
├── payment-service/         # Payment processing
├── notification-service/    # Email/SMS notifications
├── analytics-service/       # Spending analytics
├── docs/                    # Documentation
├── docker-compose.yml       # Docker services configuration
├── build.sh                 # Build script
└── README.md               # Project overview
```

### Service Architecture

```
┌─────────────┐
│   Frontend  │
└──────┬──────┘
       │
┌──────▼──────────┐
│   API Gateway   │
└──────┬──────────┘
       │
┌──────▼──────────┐
│ Discovery Server│
└──────┬──────────┘
       │
┌──────┴──────────────────────┐
│  Microservices              │
├─────────────────────────────┤
│ • User Service              │
│ • Group Service             │
│ • Expense Service           │
│ • Settlement Service        │
│ • Payment Service           │
│ • Notification Service      │
│ • Analytics Service         │
└─────────────────────────────┘
```

## 🔧 Making Changes

### Creating a Branch

```bash
# Update your fork
git fetch upstream
git checkout main
git merge upstream/main

# Create a feature branch
git checkout -b feature/your-feature-name

# Or for bug fixes
git checkout -b fix/bug-description
```

### Branch Naming Convention

- `feature/` - New features (e.g., `feature/add-payment-recording`)
- `fix/` - Bug fixes (e.g., `fix/balance-calculation`)
- `docs/` - Documentation (e.g., `docs/api-reference`)
- `refactor/` - Code refactoring (e.g., `refactor/user-service`)
- `test/` - Adding tests (e.g., `test/expense-controller`)
- `chore/` - Maintenance tasks (e.g., `chore/update-dependencies`)

### Commit Message Guidelines

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Examples:**
```bash
feat(expense): add payment recording endpoint

Added POST /api/settlements/record endpoint to allow users to
mark settlements as paid. Includes validation and balance updates.

Closes #123

---

fix(balance): handle null values in calculation

Fixed NullPointerException when calculating balances for users
with no expenses. Now returns 0.00 instead of crashing.

Fixes #456

---

docs(api): update settlement endpoints documentation

Added examples and response schemas for settlement suggestion
endpoints in API_DOCUMENTATION.md
```

## 🔄 Pull Request Process

### Before Submitting

1. **Update your branch**
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Run tests**
   ```bash
   mvn test  # For each service
   ```

3. **Build successfully**
   ```bash
   mvn clean package
   ```

4. **Update documentation** if needed
   - Update API docs for new endpoints
   - Update README for new features
   - Add/update code comments

5. **Self-review your code**
   - Remove debug statements
   - Check for proper error handling
   - Verify code formatting

### Submitting Pull Request

1. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Create Pull Request** on GitHub

3. **Fill in the PR template:**

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Related Issue
Closes #(issue number)

## Changes Made
- Change 1
- Change 2
- Change 3

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing completed

## Screenshots (if applicable)
Add screenshots here

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] No new warnings generated
- [ ] Tests pass locally
```

4. **Request review** from maintainers

### Review Process

- Maintainers will review within **2-3 business days**
- Address feedback by pushing new commits
- Once approved, a maintainer will merge your PR
- Your contribution will be in the next release! 🎉

## 📝 Coding Standards

### Java/Spring Boot

```java
// ✅ Good
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {
    
    private final ExpenseRepository expenseRepository;
    
    /**
     * Calculate user balance for a specific group
     * 
     * @param userId User ID
     * @param groupId Group ID
     * @return Balance amount
     */
    public BigDecimal calculateBalance(String userId, Long groupId) {
        log.info("Calculating balance for user: {} in group: {}", userId, groupId);
        
        // Fetch expenses
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        
        // Calculate balance
        BigDecimal balance = expenses.stream()
            .filter(e -> e.getPaidBy().equals(userId))
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        log.debug("Balance calculated: {}", balance);
        return balance;
    }
}
```

**Follow:**
- Use `@Slf4j` for logging
- Use `@RequiredArgsConstructor` for dependency injection
- Add JavaDoc for public methods
- Use meaningful variable names
- Keep methods focused (Single Responsibility)
- Use Java 17 features (records, streams, etc.)

### REST API Guidelines

```java
// ✅ Good REST API Design
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    
    // GET for retrieving resources
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpense(@PathVariable Long id) {
        // Implementation
    }
    
    // POST for creating resources
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @Valid @RequestBody CreateExpenseRequest request) {
        // Implementation
    }
    
    // PUT for full updates
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExpenseRequest request) {
        // Implementation
    }
    
    // PATCH for partial updates
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        // Implementation
    }
    
    // DELETE for removing resources
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable Long id) {
        // Implementation
    }
}
```

**Follow:**
- Use proper HTTP methods (GET, POST, PUT, PATCH, DELETE)
- Return meaningful status codes
- Use `@Valid` for request validation
- Consistent response format (ApiResponse wrapper)
- Use path variables for resource IDs
- Use query parameters for filters/pagination

### Error Handling

```java
// ✅ Good error handling
@Service
public class GroupService {
    
    public GroupResponse getGroup(Long groupId, String userId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Group not found with id: " + groupId));
                
        if (!group.isMember(userId)) {
            throw new UnauthorizedException(
                "User is not a member of this group");
        }
        
        return convertToResponse(group);
    }
}

// Custom exceptions
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

## 🧪 Testing Guidelines

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {
    
    @Mock
    private ExpenseRepository expenseRepository;
    
    @InjectMocks
    private ExpenseService expenseService;
    
    @Test
    @DisplayName("Should calculate balance correctly for user with expenses")
    void shouldCalculateBalanceCorrectly() {
        // Given
        String userId = "user123";
        Long groupId = 1L;
        List<Expense> expenses = List.of(
            createExpense(userId, new BigDecimal("50.00")),
            createExpense(userId, new BigDecimal("30.00"))
        );
        
        when(expenseRepository.findByGroupId(groupId))
            .thenReturn(expenses);
        
        // When
        BigDecimal balance = expenseService.calculateBalance(userId, groupId);
        
        // Then
        assertThat(balance).isEqualByComparingTo(new BigDecimal("80.00"));
        verify(expenseRepository).findByGroupId(groupId);
    }
}
```

**Guidelines:**
- Write tests for all public methods
- Use descriptive test names
- Follow Arrange-Act-Assert pattern
- Mock external dependencies
- Aim for >80% code coverage

### Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldCreateExpenseSuccessfully() throws Exception {
        CreateExpenseRequest request = CreateExpenseRequest.builder()
            .description("Dinner")
            .amount(new BigDecimal("50.00"))
            .groupId(1L)
            .build();
        
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", "user123")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.description").value("Dinner"));
    }
}
```

## 📚 Documentation

### Code Documentation

- Add JavaDoc for public classes and methods
- Explain WHY, not just WHAT
- Document assumptions and edge cases
- Include examples for complex logic

### API Documentation

Update `docs/FRONTEND_API_REFERENCE.md` for new endpoints:

```markdown
### POST `/api/expenses`

**Purpose:** Create a new expense

**Authentication:** Required (JWT token)

**Request Body:**
\`\`\`json
{
  "description": "Dinner",
  "amount": 50.00,
  "groupId": 1,
  "category": "FOOD",
  "splits": [
    {"userId": "user1", "amount": 25.00},
    {"userId": "user2", "amount": 25.00}
  ]
}
\`\`\`

**Success Response (201):**
\`\`\`json
{
  "success": true,
  "message": "Expense created successfully",
  "data": {
    "id": 1,
    "description": "Dinner",
    "amount": 50.00
  }
}
\`\`\`
```

## 🎨 UI/Frontend Guidelines

### Component Structure

```typescript
// expense-form.component.ts
@Component({
  selector: 'app-expense-form',
  templateUrl: './expense-form.component.html',
  styleUrls: ['./expense-form.component.css']
})
export class ExpenseFormComponent implements OnInit {
  expenseForm: FormGroup;
  
  constructor(
    private fb: FormBuilder,
    private expenseService: ExpenseService
  ) {}
  
  ngOnInit(): void {
    this.initForm();
  }
  
  private initForm(): void {
    this.expenseForm = this.fb.group({
      description: ['', Validators.required],
      amount: ['', [Validators.required, Validators.min(0)]],
      groupId: ['', Validators.required]
    });
  }
  
  onSubmit(): void {
    if (this.expenseForm.valid) {
      this.expenseService.createExpense(this.expenseForm.value)
        .subscribe({
          next: (response) => console.log('Success'),
          error: (error) => console.error('Error', error)
        });
    }
  }
}
```

## 🏆 Recognition

Contributors will be:
- Listed in our `CONTRIBUTORS.md` file
- Mentioned in release notes
- Added to GitHub contributors page
- Featured in our project website (coming soon!)

## 📬 Getting Help

- **Questions?** Open a [Discussion](https://github.com/Dhruv1030/SplitIt/discussions)
- **Stuck?** Join our [Discord](#) (coming soon!)
- **Bug?** Open an [Issue](https://github.com/Dhruv1030/SplitIt/issues)
- **Email:** dhruv03.work@gmail.com

## 📄 License

By contributing, you agree that your contributions will be licensed under the same license as the project (see [LICENSE](LICENSE) file).

---

## 🌟 Thank You!

Your contributions make SplitIt better for everyone. Whether you're fixing bugs, adding features, improving docs, or helping others - **every contribution matters**! 

Happy coding! 🚀

---

**Maintained with ❤️ by [Dhruv Patel](https://github.com/Dhruv1030) and the SplitIt community**
