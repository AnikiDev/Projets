library(ggplot2)
library(dplyr)

# Lire le fichier CSV généré par ton premier code Java
data <- read.csv("U:/qualiteDev/Projets/scaling_results.csv")

# --- STRONG SCALING ---
strong_data <- subset(data, testType == "strong")

# Calcul du speedup : référence = temps à 1 processeur
strong_data <- strong_data %>%
  group_by(nTot) %>%
  mutate(speedup = median_ms[nbP == 1] / median_ms)

ggplot(strong_data, aes(x = nbP, y = speedup, color = factor(nTot))) +
  geom_line() +
  geom_point() +
  geom_line(aes(x = nbP, y = nbP), color = "black", linetype = "dashed") +  # courbe idéale
  labs(
    title = "Strong Scaling (Speedup)",
    x = "Nombre de processeurs",
    y = "Speedup",
    color = "nTot"
  ) +
  theme_minimal()

# --- WEAK SCALING ---
weak_data <- subset(data, testType == "weak")

# Calcul du speedup : référence = temps à 1 processeur
weak_data <- weak_data %>%
  mutate(speedup = median_ms[nbP == 1] / median_ms)

ggplot(weak_data, aes(x = nbP, y = speedup)) +
  geom_line(color = "red") +
  geom_point(color = "darkred") +
  geom_hline(yintercept = 1, linetype = "dashed", color = "black") +  # ligne idéale
  labs(
    title = "Weak Scaling (Speedup)",
    x = "Nombre de processeurs",
    y = "Speedup"
  ) +
  theme_minimal()
