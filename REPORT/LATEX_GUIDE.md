# � LaTeX Report Guide

## �️ Prerequisites & Setup

### Required Software

1. **MiKTeX (Windows)**
   ```powershell
   # Download from: https://miktex.org/download
   # Or install via Chocolatey:
   choco install miktex -y
   ```

2. **VS Code with LaTeX Workshop Extension (Recommended)**
   ```bash
   # Install VS Code: https://code.visualstudio.com/
   # Install extension: james-yu.latex-workshop
   ```

3. **Perl (Required for advanced features)**
   ```powershell
   # Install Strawberry Perl
   choco install strawberryperl -y
   
   # Add to PATH if needed
   $currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
   [Environment]::SetEnvironmentVariable("PATH", "$currentPath;C:\Strawberry\perl\bin", "User")
   ```

### Verify Installation

```cmd
# Check if pdflatex is available
pdflatex --version

# Expected output:
# pdfTeX 3.141592653-2.6-1.40.27 (MiKTeX 25.4)
```

## 📂 File Structure

```
REPORT/
├── sport_ontology_report.tex  # Main LaTeX source file
├── sport_ontology_report.pdf  # Generated PDF output
├── images/                    # Image assets (if any)
└── LATEX_GUIDE.md            # This guide
```

## 🚀 Quick Start Methods

### Method 1: VS Code (Recommended)

1. **Open the file:**
   ```bash
   code sport_ontology_report.tex
   ```

2. **Edit content:**
   - LaTeX Workshop provides syntax highlighting
   - Auto-completion for LaTeX commands
   - Error detection and warnings

3. **Compile to PDF:**
   - **Option A:** Press `Ctrl+Alt+B` (Build)
   - **Option B:** Click "Build LaTeX project" in command palette (`Ctrl+Shift+P`)
   - **Option C:** Use the LaTeX Workshop sidebar ▶️ button

### Method 2: Command Line (Used in This Project)

1. **Navigate to REPORT folder:**
   ```cmd
   cd C:\Users\[username]\Desktop\RCR\projects\sport_ontology\REPORT
   ```

2. **Compile with pdflatex (Primary method used):**
   ```cmd
   pdflatex -interaction=nonstopmode sport_ontology_report.tex
   ```

3. **Check output:**
   ```cmd
   # View page count (command used frequently)
   pdfinfo sport_ontology_report.pdf | findstr "Pages"
   
   # Open PDF
   start sport_ontology_report.pdf
   ```

### Method 3: TeXworks GUI

1. **Open TeXworks**
2. **File → Open:** `sport_ontology_report.tex`
3. **Set typeset to:** `pdfLaTeX`
4. **Click the green arrow** to compile

## 📝 Editing the Report

### Document Structure (LLNCS Format)

```latex
% Document class - Lecture Notes in Computer Science
\documentclass[runningheads]{llncs}

% Essential packages used in this project
\usepackage[T1]{fontenc} 
\usepackage{graphicx}
\usepackage{listings}
\usepackage{amsmath,amssymb}
\usepackage{xcolor}
\usepackage{booktabs}  % Professional tables
\usepackage{url}

% Title and authors
\title{Sport Ontology OBDA System}
\author{Your Name}

\begin{document}
\maketitle

% Content sections
\section{Introduction}
\section{Purpose and Scope of the Ontology}
% ... more sections

\end{document}
```

### Common LaTeX Commands Used in This Project

#### Text Formatting
```latex
\textbf{Bold text}              # Used for emphasis
\textit{Italic text}            # Used for terms
\texttt{Monospace/code text}    # Used for technical terms
\emph{Emphasized text}          # Used for concepts
```

#### Sections and Subsections
```latex
\section{Main Section}
\subsection{Subsection}
\subsubsection{Sub-subsection}
```

#### Lists (Frequently Used)
```latex
% Bullet points (used extensively)
\begin{itemize}
    \item First item
    \item Second item
\end{itemize}

% Numbered list
\begin{enumerate}
    \item First item
    \item Second item
\end{enumerate}
```

#### Code Blocks (Technical Content)
```latex
% Inline code
\texttt{DATABASE\_TO\_UPPER=true}

% Simple code block
\begin{verbatim}
SELECT ?player WHERE {
    ?player a :Player
}
\end{verbatim}

% Advanced code with syntax highlighting
\begin{lstlisting}[language=SQL]
SELECT * FROM player_role WHERE end_date IS NULL;
\end{lstlisting}
```

#### Tables (Professional Format)
```latex
\begin{table}[h]
\caption{Performance Comparison}
\label{tab:performance}
\begin{tabular}{@{}lll@{}}
\toprule
\textbf{Aspect} & \textbf{Database} & \textbf{Ontology} \\
\midrule
Query Speed & Fast (3-7ms) & Slower (3.8s) \\
Reasoning & Manual & Automatic \\
\bottomrule
\end{tabular}
\end{table}
```

#### Mathematical Expressions (Description Logic)
```latex
% Inline math
The formula uses $\geq$ for constraints

% Display math (used for ontology definitions)
\begin{equation}
\text{TopPlayer} \equiv \text{Player} \sqcap \exists \text{hasMarketValue}.\{v : v \geq 100,000,000\}
\end{equation}

% Special symbols used in this project:
$\equiv$  % Equivalent to
$\sqcap$  % Intersection
$\circ$   % Composition
$\geq$    % Greater than or equal
$\rightarrow$ % Implies
```

## 🔧 Commands Used in This Project

### Primary Compilation Command
```cmd
# Main command used throughout development
pdflatex -interaction=nonstopmode sport_ontology_report.tex

# Why this specific command:
# -interaction=nonstopmode: Auto-continues on errors
# Prevents hanging on missing packages or warnings
```

### Quality Control Commands
```cmd
# Check page count (used frequently - target: <40 pages)
pdfinfo sport_ontology_report.pdf | findstr "Pages"

# Open PDF for review
start sport_ontology_report.pdf
```

### Cleanup Commands (Project Maintenance)
```cmd
# Remove auxiliary files after compilation
del sport_ontology_report.aux
del sport_ontology_report.log
del sport_ontology_report.fls
del sport_ontology_report.fdb_latexmk

# Remove all auxiliary files at once
for %i in (*.aux *.log *.fls *.fdb_latexmk) do del "%i"

# Remove minted cache (if present)
Remove-Item -Recurse -Force "*minted*" -ErrorAction SilentlyContinue
```

## 🐛 Troubleshooting (Actual Issues Encountered)

### Issues Solved During Development

#### LaTeX Syntax Errors
```
! LaTeX Error: There's no line here to end.
```
**Cause:** Extra `\\` line breaks or incorrect escaping
**Solution:** 
- Check for proper escaping: `\texttt{DATABASE\_TO\_UPPER}`
- Remove unnecessary line breaks
- Verify balanced braces `{}`

#### Package Installation
```
! LaTeX Error: File `booktabs.sty' not found.
```
**Solution:** MiKTeX auto-installs missing packages when prompted

#### Page Limit Management
**Issue:** Report growing beyond 40-page target
**Solution:**
```cmd
# Monitor page count after each major addition
pdfinfo sport_ontology_report.pdf | findstr "Pages"
```

#### Special Character Handling
**Issue:** Underscores in technical terms breaking compilation
**Solution:**
```latex
% Wrong:
\texttt{DATABASE_TO_UPPER}

% Correct:
\texttt{DATABASE\_TO\_UPPER}
```

### Emergency Recovery Steps
```cmd
# If compilation fails completely:
1. Make backup: copy sport_ontology_report.tex sport_ontology_report_backup.tex
2. Check .log file for specific error
3. Comment out problematic sections using %
4. Recompile section by section to isolate issues
```

## � Project-Specific Configuration

### Document Settings
- **Format:** LLNCS (Lecture Notes in Computer Science)
- **Page Limit:** 40 pages maximum
- **Target:** Academic conference submission quality

### Package Dependencies Used
```latex
\usepackage[T1]{fontenc}     % Font encoding
\usepackage{booktabs}        % Professional tables  
\usepackage{listings}        % Code syntax highlighting
\usepackage{amsmath,amssymb} % Mathematical expressions
\usepackage{graphicx}        % Image handling
\usepackage{url}             % URL formatting
\usepackage{xcolor}          % Color support
```

### Special Formatting Requirements
```latex
% Math symbols for Description Logic
$\equiv$ $\sqcap$ $\circ$ $\geq$ $\rightarrow$

% Technical terms formatting
\texttt{technical\_terms\_with\_underscores}

% Emphasis patterns
\textbf{Key Concepts:} Description following
```

## ✅ Complete Workflow Summary

### Development Workflow Used
1. **Edit:** Open `sport_ontology_report.tex` in VS Code
2. **Compile:** Run `pdflatex -interaction=nonstopmode sport_ontology_report.tex`
3. **Check:** `pdfinfo sport_ontology_report.pdf | findstr "Pages"`
4. **Review:** `start sport_ontology_report.pdf`
5. **Clean:** Remove auxiliary files when done
6. **Repeat:** Continue editing and testing

### Final Export Process
```cmd
# Final compilation
pdflatex -interaction=nonstopmode sport_ontology_report.tex

# Verify quality
pdfinfo sport_ontology_report.pdf | findstr "Pages"

# Clean repository
del *.aux *.log *.fls *.fdb_latexmk
```

## 🔗 Resources & References

- [LaTeX Wikibook](https://en.wikibooks.org/wiki/LaTeX)
- [LLNCS Template Guide](https://www.springer.com/gp/computer-science/lncs/conference-proceedings-guidelines)
- [MiKTeX Documentation](https://miktex.org/docs)
- [LaTeX Mathematical Symbols](https://oeis.org/wiki/List_of_LaTeX_mathematical_symbols)

## 📞 Quick Reference Card

### Essential Commands (Copy-Paste Ready)
```cmd
# Navigate to folder
cd C:\Users\[username]\Desktop\RCR\projects\sport_ontology\REPORT

# Compile (main command)
pdflatex -interaction=nonstopmode sport_ontology_report.tex

# Check pages  
pdfinfo sport_ontology_report.pdf | findstr "Pages"

# Open result
start sport_ontology_report.pdf

# Clean up
for %i in (*.aux *.log *.fls *.fdb_latexmk) do del "%i"
```

This guide reflects the actual setup and commands used successfully in developing the Sport Ontology OBDA System report!

