#!/bin/bash

echo "🔄 Forcing VS Code to reload Java projects..."

# Remove any cached Java workspace data
echo "Cleaning Java workspace cache..."
rm -rf ~/.vscode/extensions/redhat.java-*/server/bin
rm -rf ~/.vscode/extensions/redhat.java-*/server/cache

echo ""
echo "✅ Cache cleared!"
echo ""
echo "📝 Next steps:"
echo "1. Close this terminal"
echo "2. In VS Code, press Cmd+Shift+P (or Ctrl+Shift+P)"
echo "3. Type: Java: Clean Java Language Server Workspace"
echo "4. Select it and click 'Reload and delete'"
echo "5. Wait 30-60 seconds for Java to reload"
echo "6. The 43 problems should disappear!"
echo ""
echo "OR simply:"
echo "1. Close VS Code completely (Cmd+Q)"
echo "2. Reopen VS Code"
echo "3. Open SplitIt folder"
echo "4. Wait for Java Language Server to load"
