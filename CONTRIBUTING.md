# Contributing to IonAPI Plugin Template

Thank you for your interest in contributing! This document provides guidelines for contributing to the IonAPI Plugin Template.

## 🚀 Getting Started

1. **Fork the repository**
2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/ion-plugin-template.git
   cd ion-plugin-template
   ```
3. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

## 📋 Development Setup

### Prerequisites
- Java 21 or higher
- Gradle 8.0+
- Git

### Build IonAPI Locally
```bash
cd ../IonAPI
./gradlew publishToMavenLocal
cd ../ion-plugin-template
```

### Build the Template
```bash
./gradlew clean shadowJar
```

### Run Tests
```bash
./gradlew test
```

## 🎯 What to Contribute

### Good First Issues
- Documentation improvements
- Code examples
- Bug fixes
- Performance optimizations

### Feature Additions
- New command examples
- Additional GUI menus
- Database entity examples
- Configuration options

### What We're Looking For
- ✅ Clean, readable code
- ✅ Proper error handling
- ✅ Javadoc comments
- ✅ Example usage in README
- ✅ Follows existing code style

## 📝 Code Style

### Java Conventions
- Use 4 spaces for indentation
- Follow standard Java naming conventions
- Add Javadoc for public methods
- Keep methods focused and small

### Example
```java
/**
 * Teleports a player to spawn with cooldown check.
 * 
 * @param player The player to teleport
 * @return true if teleport was successful
 */
public boolean teleportToSpawn(Player player) {
    if (cooldowns.isOnCooldown(player.getUniqueId())) {
        return false;
    }
    // Implementation...
    return true;
}
```

## 🧪 Testing

- Add tests for new features
- Ensure existing tests pass
- Test with Paper 1.20.4+
- Verify hot-reload works

## 📤 Submitting Changes

1. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: Add new feature description"
   ```

2. **Use conventional commits**
   - `feat:` New feature
   - `fix:` Bug fix
   - `docs:` Documentation
   - `refactor:` Code refactoring
   - `test:` Adding tests
   - `chore:` Maintenance

3. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

4. **Create a Pull Request**
   - Describe your changes
   - Reference any related issues
   - Add screenshots if applicable

## 🐛 Reporting Bugs

### Before Reporting
- Check existing issues
- Verify it's not a configuration issue
- Test with latest version

### Bug Report Template
```markdown
**Description**
Clear description of the bug

**Steps to Reproduce**
1. Step one
2. Step two
3. ...

**Expected Behavior**
What should happen

**Actual Behavior**
What actually happens

**Environment**
- Java Version: 21
- Paper Version: 1.20.4
- IonAPI Version: 1.4.0
- Plugin Version: 1.0.0
```

## 💡 Feature Requests

We welcome feature suggestions! Please:
- Check if it already exists
- Explain the use case
- Provide example code if possible
- Consider if it fits the template's scope

## 📜 Code of Conduct

### Our Standards
- Be respectful and inclusive
- Accept constructive criticism
- Focus on what's best for the community
- Show empathy towards others

### Unacceptable Behavior
- Harassment or discrimination
- Trolling or insulting comments
- Publishing private information
- Unprofessional conduct

## 🔍 Review Process

1. **Automated Checks**
   - Build must pass
   - Tests must pass
   - Code style checks

2. **Manual Review**
   - Code quality
   - Documentation
   - Functionality

3. **Feedback**
   - Address review comments
   - Make requested changes
   - Re-request review

## 📚 Resources

- [IonAPI Documentation](https://github.com/mattbaconz/IonAPI)
- [Paper API Docs](https://docs.papermc.io/)
- [Discord Community](https://discord.com/invite/VQjTVKjs46)

## ❓ Questions?

- Join our [Discord](https://discord.com/invite/VQjTVKjs46)
- Open a [Discussion](https://github.com/mattbaconz/ion-plugin-template/discussions)
- Check existing [Issues](https://github.com/mattbaconz/ion-plugin-template/issues)

## 🙏 Thank You!

Your contributions make this template better for everyone. We appreciate your time and effort!

---

**Happy Coding!** 🚀
