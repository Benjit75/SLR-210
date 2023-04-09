from matplotlib import pyplot as plt

latency_tle_1_a_0 = [1034, 1030, 1105]
latency_tle_1_a_01 = [1068, 1001, 1169]
latency_tle_1_a_1 = [943, 1146, 1185]

N = [3, 10, 100]
tle = [0.5, 1, 1.5, 2]
alpha = [0, 0.1, 1]

# Plotting latency vs N for tle = 1 and alpha = 0
plt.plot(N, latency_tle_1_a_0, 'r-o', label="alpha = 0")
plt.plot(N, latency_tle_1_a_01, 'b-o', label="alpha = 0.1")
plt.plot(N, latency_tle_1_a_1, 'g-o', label="alpha = 1")
plt.title('Latency vs N for tle = 1')
plt.xlabel('N')
plt.ylabel('Latency')
plt.legend()
plt.grid(True)
plt.savefig('latency_vs_N.png')
# plt.show()

# Plotting latency vs tle for N = 10 and alpha = 0
latency_N_10_a_0 = [1148, 1046, 1120, 996]
latency_N_10_a_01 = [1040, 1016, 1149, 1171]
latency_N_10_a_1 = [1195, 1179, 1107, 1012]
plt.figure()
plt.plot(tle, latency_N_10_a_0, 'r-o', label="alpha = 0")
plt.plot(tle, latency_N_10_a_01, 'b-o', label="alpha = 0.1")
plt.plot(tle, latency_N_10_a_1, 'g-o', label="alpha = 1")
plt.title('Latency vs tle for N = 10')
plt.legend()
plt.xlabel('tle')
plt.ylabel('Latency')
plt.grid(True)
plt.savefig('latency_vs_tle.png')
# plt.show()