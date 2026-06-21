import React from 'react';
import {View, Text, StyleSheet} from 'react-native';

function App(): React.JSX.Element {
  return (
    <View style={styles.container}>
      <Text style={styles.text}>App Chal Gaya Bhai 🔥</Text>
      <Text style={styles.subtext}>Ab Fused Location banate hain</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#111',
  },
  text: {
    fontSize: 24,
    color: 'white',
    fontWeight: 'bold',
  },
  subtext: {
    fontSize: 16,
    color: '#888',
    marginTop: 8,
  },
});

export default App;